package algorithms

import scala.util.Random
import scala.collection.immutable.HashMap
import scala.collection.mutable.{HashMap => MutableHashMap}

import java.nio.file.{Files, Paths}
import java.io.{BufferedWriter, FileWriter}
import java.lang.IllegalArgumentException

import de.lmu.ifi.dbs.elki.distance.distancevalue.{DoubleDistance}
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.{MaximumDistanceFunction, EuclideanDistanceFunction}
import de.lmu.ifi.dbs.elki.database.ids.distance.DistanceDBIDList
import de.lmu.ifi.dbs.elki.database.ids.{DBIDRef, DBIDIter, DBID, DBIDUtil}
import de.lmu.ifi.dbs.elki.database.Database
import de.lmu.ifi.dbs.elki.database.relation.Relation
import de.lmu.ifi.dbs.elki.data.{DoubleVector}
import de.lmu.ifi.dbs.elki.data.`type`.TypeUtil
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar._
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialEntry
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.strategies.bulk.SortTileRecursiveBulkSplit

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
    val rTreePath = "tplSimulation/rTree.csv"
    (s"Reference Points: ${refPoints.mkString(",")} \n")

    /*
     * 1. Preparation phase
     */
    Log.appendln(s"\n1. Start preparation phase (create: embedding, DB, R*Tree, query object)")
    val timeAlgorithmPreparation = TimeDiff(System.currentTimeMillis)

    /*
     *    1.1 Create embedding
     */
    Log.append(s"  1.1 Creating embedding..")
    val timeCreateEmbedding = TimeDiff(System.currentTimeMillis)

    val refPointDistances: HashMap[SVertex, IndexedSeq[Double]] = createEmbedding(sGraph, refPoints) // key: Knoten, value: Liste mit Distanzen zu Referenzpunkte (? evtl: Flag ob Objekt auf Knoten)

    timeCreateEmbedding.tEnd = System.currentTimeMillis
    Log.appendln(s" done in $timeCreateEmbedding")


    /*
     *    1.2 Create memory database (after writing CSV file)
     */
    Log.append(s"  - Creating CSV file..")
    val timeCreateCSV = TimeDiff(System.currentTimeMillis)

    writeRTreeCSVFile(refPointDistances, rTreePath)
//    Utils.generateRandomCSVFile(refPoints.size, 100, rTreePath) // dimensions = numRefPoints, number of random vectors to be created = 100

    timeCreateCSV.tEnd = System.currentTimeMillis
    Log.appendln(s" done in $timeCreateCSV")

    Log.append(s"  - Creating file based Database..")
    val timeCreateDB = TimeDiff(System.currentTimeMillis)

    val db: List[Database]                      = Utils.createDatabase(rTreePath).toList
    val relation: Relation[DoubleVector]        = db(0).getRelation(TypeUtil.NUMBER_VECTOR_FIELD)

    timeCreateDB.tEnd = System.currentTimeMillis
    Log.appendln(s" done in $timeCreateDB")



    /*
     *    1.3 Create RStar tree index
     */
    Log.append(s"  - Creating R*Tree index (entries: " + relation.size() + ", page size: " + rStarTreePageSize + " bytes).. ");
    val timeCreateRStarTree = TimeDiff(System.currentTimeMillis)

    val rStarTree: RStarTreeIndex[DoubleVector] = Utils.createRStarTree(relation, rStarTreePageSize)

    timeCreateRStarTree.tEnd = System.currentTimeMillis
    Log.appendln(s"  done in $timeCreateRStarTree")

    /*
     *    1.4 Create TPL rknn query and query object
     */
    Log.append(s"  - Creating EmbeddedTPLQuery object and query object ..")
    val timeCreateQuery = TimeDiff(System.currentTimeMillis)

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

    timeCreateQuery.tEnd = System.currentTimeMillis
    Log.appendln(s" done in $timeCreateQuery")


    timeAlgorithmPreparation.tEnd = System.currentTimeMillis
    Log.appendln(s"Algorithm preparation done in $timeAlgorithmPreparation \n").printFlush

    /*
     * 2. Performing embedded TPL rknn query
     */
    Log.appendln(s"2. Performing R${k}NN-query...")
    val timeTotalRknn = TimeDiff(System.currentTimeMillis)

    /*
     *    2.1 Filter-refinement in embedded space
     */
    Log.appendln(s"  2.1 Performing filter refinement in embedded space..")
    val timeFilterRefEmbedding = TimeDiff(System.currentTimeMillis)

    val distanceDBIDList: DistanceDBIDList[DoubleDistance] = tplEmbedded.getRKNNForObject(queryObject, k)

    timeFilterRefEmbedding.tEnd = System.currentTimeMillis
    Log.appendln(s"  Filter Refinement in embedded space done in $timeFilterRefEmbedding \n").printFlush

    /*
     *    2.2 Refining the TPL rknn results on graph
     */
    Log.appendln(s"  2.2 Refining candidates on graph..")
    val timeRefinementOnGraph = TimeDiff(System.currentTimeMillis)


    /*
     *        2.2.1 Mapping embedded candidates from DB to Graph
     */
    Log.append(s"    - Mapping candidates from DB to graph..")
    val timeMappingFromDBtoGraph = TimeDiff(System.currentTimeMillis)

    var filterRefinementResultsEmbedding = IndexedSeq.empty[(SVertex, Double)]
    val iter = distanceDBIDList.iter()
    while (iter.valid()) {
      filterRefinementResultsEmbedding :+= (sGraph.getVertex(Integer.parseInt(DBIDUtil.deref(iter).toString)), iter.getDistance.doubleValue)
      iter.advance()
    }

    timeMappingFromDBtoGraph.tEnd = System.currentTimeMillis
    Log.appendln(s" done in $timeMappingFromDBtoGraph")

    /*
     *        2.2.2 Refinement on graph
     */
    Log.append(s"    - Performing refinement on graph..")
    val timePerformRefinementOnGraph = TimeDiff(System.currentTimeMillis)
    // If q doesn't contain an object, give it an object so that it will be found by the knn algorithm
    if (!sQ.containsObject)
      sQ.setObjectId(sGraph.getAllVertices.size)

    val allkNNs = filterRefinementResultsEmbedding map ( vd =>
      (vd._1, Eager.rangeNN(sGraph, vd._1, k, Double.PositiveInfinity))
    )
    // If q didn't contain an object before, remove the previously inserted object
    if (sQ.getObjectId == sGraph.getAllVertices.size)
      sQ.setObjectId(SVertex.NO_OBJECT)

    val rKnns = allkNNs collect {
      case (v, knns) if knns map( _._1 ) contains sQ => new VD(v, knns.find(y => (y._1 equals sQ)).get._2)
    }

    timePerformRefinementOnGraph.tEnd = System.currentTimeMillis
    Log.appendln(s" done in $timePerformRefinementOnGraph")

    timeRefinementOnGraph.tEnd = System.currentTimeMillis
    Log.appendln(s"  Refinement of candidates on graph done in $timeRefinementOnGraph")

    timeTotalRknn.tEnd = System.currentTimeMillis
    Log.appendln(s"R${k}NN query performed in $timeTotalRknn \n")

    rKnns
  }


  /**
   * Takes a list of vertices and creates numRefPoints random reference points.
   * @return A list of random vertices
   */
  def createRefPoints(vertices: Seq[SVertex], numRefPoints: Integer): Seq[SVertex] =
    new Random(System.currentTimeMillis).shuffle(vertices) take numRefPoints


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
   * Takes a map of Vertex -> Distances and writes it as a CSV file to the given path as an RTree
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
    val iter = relation.getDBIDs.iter
    while (iter.valid) {
      if (iter.internalGetIndex == vertex.id)
        return iter
      iter.advance()
    }
    throw new IllegalArgumentException("relation not valid or id not found.")
  }

  def getVertexFromDBID(dbid: DBID, refPointDistances: HashMap[SVertex, IndexedSeq[Double]]): SVertex = {
    refPointDistances.find(_._1.id == dbid.internalGetIndex) match {
      case Some(x) => x._1
      case None    => throw new IllegalArgumentException("DBID id not found. This is a severe error: dbid.internalGetIndex does not correspond to the graph's ID!")
    }
  }



  /*
  def getReverseKNearestNeighbours(graph: SGraph, q: SVertex, k: Int): IndexedSeq[VD] = {
    var RkNN_q         = IndexedSeq.empty[VD]
    RkNN_q.sortWith((x,y) => (x._2 < y._2) || (x._2 == y._2) && (x._1.id < y._1.id))
  }
  */
}