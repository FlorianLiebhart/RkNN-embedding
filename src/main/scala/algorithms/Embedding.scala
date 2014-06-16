package algorithms

import scala.util.Random
import scala.collection.immutable.HashMap
import scala.collection.mutable.{HashMap => MutableHashMap}
import scala.collection.JavaConversions._

import java.nio.file.{Files, Paths}
import java.io.{BufferedWriter, FileWriter}
import java.lang.IllegalArgumentException

import de.lmu.ifi.dbs.elki.database.ids.{DBIDRef, DBID, DBIDUtil}
import de.lmu.ifi.dbs.elki.database.Database
import de.lmu.ifi.dbs.elki.database.relation.Relation
import de.lmu.ifi.dbs.elki.data.{DoubleVector}
import de.lmu.ifi.dbs.elki.data.`type`.TypeUtil
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar._

import elkiTPL.{EmbeddedTPLQuery, Utils}
import graph.{SVertex, SGraph}
import util.Utils.{VD, TimeDiff}
import util.Log

/**
 * @author fliebhart
 */
object Embedding {


  /**
   *
   * @param sGraph
   * @param sQ
   * @param k
   * @param refPoints
   * @param rStarTreePageSize Determines how many points fit into one page. Felix: Mostly between 1024 und 8192 byte; Erich recommendation: 25*8*dimensions (=> corresponds to around 25 entries/page)
   * @return
   */
  def embeddedRkNNs(sGraph: SGraph, sQ: SVertex, k: Int, refPoints: Seq[SVertex], rStarTreePageSize: Int): IndexedSeq[(SVertex, Double)] = {
    val rTreePath        = "tplSimulation/rTree.csv"
    val numberOfVertices = sGraph.getAllVertices.size
    Log.appendln(s"\nReference Points: ${refPoints.mkString(",")} \n")

    /*
     * 1. Preparation phase
     */
    Log.appendln(s"1. Start preparation phase (create: embedding, DB, R*Tree, query object)")
    val timeAlgorithmPreparation = TimeDiff()

    /*
     *    1.1 Create embedding
     */
    Log.append(s"  1.1 Creating embedding..")
    val timeCreateEmbedding = TimeDiff()

//    makesure(refPoints.filterNot(_.containsObject).isEmpty, "All reference points must contain objects!")
//    makesure(sQ.containsObject, "The query point must contain an object!")
    val refPointDistances: HashMap[SVertex, IndexedSeq[Double]] = createEmbedding(sGraph, refPoints) // key: Knoten, value: Liste mit Distanzen zu Referenzpunkte (? evtl: Flag ob Objekt auf Knoten)
//    val refPointDistancesContainingObjects = refPointDistances.filter(x => x._1.containsObject)

    timeCreateEmbedding.end
    Log.appendln(s" done in $timeCreateEmbedding")


    /*
     *    1.2 Write CSV + Create memory database
     */
    Log.append(s"  - Creating CSV file..")
    val timeCreateCSV = TimeDiff()

    writeRTreeCSVFile(refPointDistances, rTreePath)
//    Utils.generateRandomCSVFile(refPoints.size, 100, rTreePath) // dimensions = numRefPoints, number of random vectors to be created = 100

    timeCreateCSV.end
    Log.appendln(s" done in $timeCreateCSV")

    Log.append(s"  - Creating file based Database..")
    val timeCreateDB = TimeDiff()

    val db: List[Database]               = Utils.createDatabase(rTreePath).toList
    val relation: Relation[DoubleVector] = db(0).getRelation(TypeUtil.NUMBER_VECTOR_FIELD)

    timeCreateDB.end
    Log.appendln(s" done in $timeCreateDB")



    /*
     *    1.3 Create RStar tree index
     */
    Log.append(s"  - Creating R*Tree index (entries: " + relation.size() + ", page size: " + rStarTreePageSize + " bytes)..");
    val timeCreateRStarTree = TimeDiff()

    val rStarTree: RStarTreeIndex[DoubleVector] = Utils.createRStarTree(relation, rStarTreePageSize)

    timeCreateRStarTree.end
    Log.appendln(s" done in $timeCreateRStarTree")

    /*
     *    1.4 Create TPL rknn query and query object
     */
    Log.append(s"  - Creating EmbeddedTPLQuery object and query object ..")
    val timeCreateQuery = TimeDiff()

    val tplEmbedded                             = new EmbeddedTPLQuery(rStarTree, relation)
    val queryObject: DoubleVector               = relation.get(getDBIDRefFromVertex(relation, sQ))
    // Generate random query point
    /*
    val coordinates: Array[Double] = new Array[Double](refPoints.size)
      for (i <- 0 to refPoints.size-1) {
      coordinates(i) = Math.random()
    }
    val queryObject: DoubleVector = new DoubleVector(coordinates)
    Log.appendln("Generated query object: " + queryObject)

    // random query object from the database
    val queryObject: DoubleVector = relation.get(Utils.getRandomDBObject(relation))
    Log.appendln(s"Random query object from database: $queryObject\n")
    */

    timeCreateQuery.end
    Log.appendln(s" done in $timeCreateQuery")

    timeAlgorithmPreparation.end
    Log.appendln(s"Algorithm preparation done in $timeAlgorithmPreparation \n").printFlush

    /*
     * 2. Performing embedded TPL rknn query
     */
    Log.appendln(s"2. Performing R${k}NN-query...")
    val timeTotalRknn = TimeDiff()
    /*
     *    2.1 Filter-refinement in embedded space
     */
    Log.appendln(s"  2.1 Performing filter refinement in embedded space..")
    val timeFilterRefEmbedding = TimeDiff()

    val embeddingTPLResultDBIDs: Seq[DBID] = tplEmbedded.filterRefinement(queryObject, k)

    timeFilterRefEmbedding.end
    Log.appendln(s"  Filter Refinement in embedded space done in $timeFilterRefEmbedding \n").printFlush

    /*
     *    2.2 Refining the TPL rknn results on graph
     */
    Log.appendln(s"  2.2 Refining candidates on graph..")
    val timeRefinementOnGraph = TimeDiff()
    /*
     *        2.2.1 Mapping embedded candidates from DB to Graph
     */
    Log.append(s"    - Mapping candidates from DB to graph..")
    val timeMappingFromDBtoGraph = TimeDiff()

    var filterRefinementResultsEmbedding = IndexedSeq.empty[SVertex]
    embeddingTPLResultDBIDs map { dbid =>
      filterRefinementResultsEmbedding :+= sGraph.getVertex(Integer.parseInt(DBIDUtil.deref(dbid).toString))
    }


    timeMappingFromDBtoGraph.end
    Log.appendln(s" done in $timeMappingFromDBtoGraph")

    /*
     *        2.2.2 Refinement on graph
     */
    Log.append(s"    - Performing refinement of ${filterRefinementResultsEmbedding.size} candidates on graph..")
    val timePerformRefinementOnGraph = TimeDiff()
    // If q doesn't contain an object, give it an object so that it will be found by the knn algorithm
    if (!sQ.containsObject)
      sQ.setObjectId(numberOfVertices)

    val allkNNs = filterRefinementResultsEmbedding map ( vertex =>
      (vertex, Eager.rangeNN(sGraph, vertex, k, Double.PositiveInfinity))
    )
    // If q didn't contain an object before, remove the previously inserted object
    if (sQ.getObjectId == numberOfVertices)
      sQ.setObjectId(SVertex.NO_OBJECT)

    val rKnns = allkNNs collect {
      case (v, knns) if knns map( _._1 ) contains sQ => new VD(v, knns.find(y => (y._1 equals sQ)).get._2)
    }

    timePerformRefinementOnGraph.end
    Log.appendln(s" done in $timePerformRefinementOnGraph")

    timeRefinementOnGraph.end
    Log.appendln(s"  Refinement of candidates on graph done in $timeRefinementOnGraph")

    timeTotalRknn.end
    Log.appendln(s"R${k}NN query performed in $timeTotalRknn \n")

    rKnns.sortWith((x,y) => (x._2 < y._2) || (x._2 == y._2) && (x._1.id < y._1.id))
  }


  /**
   * Takes a list of vertices and creates numRefPoints random reference points.
   * @return A list of random vertices
   */
  def createRefPoints(vertices: Seq[SVertex], numRefPoints: Integer, qID: Integer): Seq[SVertex] =
    new Random(System.currentTimeMillis).shuffle(vertices.filterNot(_.id == qID)) take numRefPoints


  /**
   * Berechnet Distanzen zwischen allen Punkten durch distFunction zu den reference points
   * @return A Map with key: Node, Val: Distances to all reference points
   */
  def createEmbedding(sGraph: SGraph, refPoints: Seq[SVertex]): HashMap[SVertex, IndexedSeq[Double]] = {
    //  // pure functional, immutable implementation: (maps still need to be merged)
    //  import scala.collection.immutable.HashMap
    //  val allRefPointDistances = refPoints.map(refPoint => Dijkstra.dijkstra(sGraph, refPoint))
    //  allRefPointDistances.map(_.foldLeft(new HashMap[SVertex, Seq[Double]]) ((x, y) => x + (y._1 -> x.get(y._1).getOrElse(Nil).:+(y._2))  ))
    val refpointDistances = MutableHashMap[SVertex, IndexedSeq[Double]]()

    for(refPoint <- refPoints){
      val allDistsFromRefPoint = Dijkstra.dijkstra(sGraph, refPoint)
      allDistsFromRefPoint map { x =>
        refpointDistances.put(
          x._1,
          (refpointDistances.get(x._1).getOrElse(Nil) :+ x._2).toIndexedSeq
        )
      }
    }
    HashMap(refpointDistances.toSeq:_*)
  }


  /**
   * Takes a map[Vertex -> Distances] as vectors and writes these vectors to the given path as a csv file,
   * where each line stands for a vector, and each row for it's value in each dimension.
   * @param vectorsMap
   * @param destPath
   * @return
   */
  def writeRTreeCSVFile(vectorsMap: HashMap[SVertex, IndexedSeq[Double]], destPath: String) {
    // create directories and file if non-existent
    val pathToFile = Paths.get(destPath)
    Files.createDirectories(pathToFile.getParent)
    if (!Files.exists(pathToFile))
        Files.createFile(pathToFile)
    val fw  = new FileWriter(destPath, false) // false = overwrite current file content
    val out = new BufferedWriter(fw)

    out.write(
      vectorsMap.toSeq.sortWith(_._1.id < _._1.id).map( vector =>
        vector._2.mkString(";") //+ ";" + vector._1.id
      ) mkString "\n"
    )
    out.close()
  }


  def getDBIDRefFromVertex(relation: Relation[DoubleVector], vertex: SVertex): DBIDRef = {
    val iter = relation.iterDBIDs
    while (iter.valid) {
      if (iter.internalGetIndex == vertex.id)
        return iter
      iter.advance()
    }
    throw new IllegalArgumentException(s"relation not valid or vertex id (${vertex.id}) not found.")
  }

  def getVertexFromDBID(dbid: DBID, refPointDistances: HashMap[SVertex, IndexedSeq[Double]]): SVertex = {
    refPointDistances.find(_._1.id == dbid.internalGetIndex) match {
      case Some(x) => x._1
      case None    => throw new IllegalArgumentException("DBID id not found. This is a severe error: dbid.internalGetIndex does not correspond to the graph's ID!")
    }
  }

}