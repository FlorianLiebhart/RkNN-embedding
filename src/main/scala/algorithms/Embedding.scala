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
import util.Utils.VD
import util.CPUTimeDiff
import util.Utils.makesure
import util.{Log, Stats}

case object Embedding extends GraphRknn{

  override val name = "Embedding"


  /**
   * @param sGraph
   * @param numRefPoints
   * @param rStarTreePageSize
   * @param predefinedRefPoints In case you want to use predefined reference points. numRefPoints won't make a difference then.
   *
   * IMPORTANT NOTE: Make sure that if you already know your query point, it MUST contain an object,
   *                 since the embedding will only be created from nodes with objects
   *
   *
   * 1. Preparation phase
   */
  def createDatabaseWithIndex(sGraph: SGraph, numRefPoints: Int, rStarTreePageSize: Int, predefinedRefPoints: Seq[SVertex] = null): (Relation[DoubleVector], RStarTreeIndex[DoubleVector], MutableHashMap[String, SVertex]) = {

    val rTreePath        = "tplSimulation/rTree.csv"

    Log.appendln(s"1. Start preparation phase (create: embedding, DB, R*Tree, query object)")
    val timeAlgorithmPreparation = CPUTimeDiff()

    /*
     *    1.1 Create embedding
     */
    Log.append(s"  - Creating refpoints and embedding..")
    val timeCreateEmbedding = CPUTimeDiff()

    val refPoints: Seq[SVertex] =
      if(numRefPoints == -1)
        predefinedRefPoints
      else {
        createRefPoints(sGraph.getAllVertices, numRefPoints)
      }
    //    Log.appendln(s"\nReference Points: ${refPoints.mkString(",")} \n")

    val refPointDistances: HashMap[SVertex, IndexedSeq[Double]] = createEmbedding(sGraph, refPoints)
    val refPointDistancesContainingObjects                      = refPointDistances.filter(x => x._1.containsObject)

    timeCreateEmbedding.end
    Log.appendln(s" done in $timeCreateEmbedding")


    /*
     *    1.2 Write CSV + Create memory database
     */
    Log.append(s"  - Creating CSV file..")
    val timeCreateCSV = CPUTimeDiff()

    val dbidVertexIDMapping: MutableHashMap[String, SVertex] = writeRTreeCSVFile(refPointDistancesContainingObjects, rTreePath)

    timeCreateCSV.end
    Log.appendln(s" done in $timeCreateCSV")

    Log.append(s"  - Creating file based Database..")
    val timeCreateDB = CPUTimeDiff()

    val db      : List[Database]         = Utils.createDatabase(rTreePath).toList
    val relation: Relation[DoubleVector] = db(0).getRelation(TypeUtil.NUMBER_VECTOR_FIELD)

    timeCreateDB.end
    Log.appendln(s" done in $timeCreateDB")



    /*
     *    1.3 Create RStar tree index
     */
    Log.append(s"  - Creating R*Tree index (entries: " + relation.size() + ", page size: " + rStarTreePageSize + " bytes)..")
    val timeCreateRStarTree = CPUTimeDiff()

    val rStarTree: RStarTreeIndex[DoubleVector] = Utils.createRStarTree(relation, rStarTreePageSize)

    timeCreateRStarTree.end
    Log.appendln(s" done in $timeCreateRStarTree")

    timeAlgorithmPreparation.end
    Stats.embeddingRunTimePreparation = timeAlgorithmPreparation.diffMillis
    Log.appendln(s"Algorithm preparation done in $timeAlgorithmPreparation \n").printFlush

    (relation, rStarTree, dbidVertexIDMapping)
  }


  /**
   * From the given relation, receives the query object from q's ID and the given dbid-vertex-mapping.
   * @param relation
   * @param q
   * @param dbidVertexIDMapping
   * @throws IllegalArgumentException if q cannot be found in the relation
   * @return
   */
  def getQueryObject(relation: Relation[DoubleVector], q: SVertex, dbidVertexIDMapping: MutableHashMap[String, SVertex]): DoubleVector = {
    relation.get(getDBIDRefFromVertex(relation, q, dbidVertexIDMapping))
  }

  /**
   * @param sGraph
   * @param q
   * @param k
   * @param relation
   * @param rStarTree
   * @param queryObject
   * @param dbidVertexIDMapping
   *
   * @return List of query results
   *
   * IMPORTANT NOTE: This algorithm relies that q contains a object, and contained a query object at the time the embedding was created!
   *                 Otherwise no results will be found.
   *
   * 2. Performing embedded TPL rknn query
   */
  def rknns(sGraph: SGraph, q: SVertex, k: Int, relation: Relation[DoubleVector], queryObject: DoubleVector, rStarTree: RStarTreeIndex[DoubleVector], dbidVertexIDMapping: MutableHashMap[String, SVertex]): Seq[VD] = {
    makesure(q.containsObject, "q must contain an object!")

    Log.appendln(s"2. Performing R${k}NN-query...")
    val timeTotalRknn = CPUTimeDiff()

    /*
     *    2.1 Filter-refinement in embedded space
     */
    Log.appendln(s"  2.1 Performing filter refinement in embedded space..")
    val timeFilterRefEmbedding = CPUTimeDiff()

    val embeddingTPLResultDBIDs: Seq[DBID] = new EmbeddedTPLQuery(rStarTree, relation).filterRefinement(queryObject, k)

    timeFilterRefEmbedding.end
    Log.appendln(s"  Filter Refinement in embedded space done in $timeFilterRefEmbedding \n").printFlush

    /*
     *    2.2 Refining the TPL rknn results on graph
     */
    Log.appendln(s"  2.2 Refining candidates on graph..")
    val timeRefinementOnGraph = CPUTimeDiff()
    /*
     *        2.2.1 Mapping embedded candidates from DB to Graph
     */
    Log.append(s"    - Mapping candidates from DB to graph..")
    val timeMappingFromDBtoGraph = CPUTimeDiff()

    val filterRefinementResultsEmbedding = embeddingTPLResultDBIDs map { dbid =>
      dbidVertexIDMapping.get(DBIDUtil.deref(dbid).toString).get
    }


    timeMappingFromDBtoGraph.end
    Log.appendln(s" done in $timeMappingFromDBtoGraph")

    /*
     *        2.2.2 Refinement on graph
     */
    val candidatesToRefineOnGraph = filterRefinementResultsEmbedding.size
    Stats.nodesToVerify             = candidatesToRefineOnGraph
    Log.append(s"    - Performing refinement of ${candidatesToRefineOnGraph} candidates on graph..")
    val timePerformRefinementOnGraph = CPUTimeDiff()

    val allkNNs = filterRefinementResultsEmbedding map ( refResult =>
      (refResult, Eager.rangeNN(sGraph, refResult, k, Double.PositiveInfinity))
    )

    val rKnns = allkNNs collect {
      case (v, knns) if (knns map( _._1 ) contains q) => new VD(v, knns.find(y => (y._1 equals q)).get._2)
    }

    timePerformRefinementOnGraph.end
    Log.appendln(s" done in $timePerformRefinementOnGraph")

    timeRefinementOnGraph.end
    Log.appendln(s"  Refinement of candidates on graph done in $timeRefinementOnGraph")

    timeTotalRknn.end
    Stats.runTimeRknnQuery = timeTotalRknn.diffMillis
    Log.appendln(s"R${k}NN query performed in $timeTotalRknn \n")

    rKnns.sortWith((x,y) => (x._2 < y._2) || (x._2 == y._2) && (x._1.id < y._1.id))
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
   * Takes a map[Vertex -> Distances] as vectors and writes these vectors to the given path as a csv file,
   * where each line stands for a vector, and each row for it's value in each dimension.
   * @param vectorsMap
   * @param destPath
   * @return
   */
  def writeRTreeCSVFile(vectorsMap: HashMap[SVertex, IndexedSeq[Double]], destPath: String): MutableHashMap[String, SVertex] = {
    // create directories and file if non-existent
    val pathToFile = Paths.get(destPath)
    Files.createDirectories(pathToFile.getParent)
    if (!Files.exists(pathToFile))
        Files.createFile(pathToFile)
    val fw  = new FileWriter(destPath, false) // false = overwrite current file content
    val out = new BufferedWriter(fw)

    val dbidVertexIDMapping = MutableHashMap[String, SVertex]()
    var i = 0

    out.write(
      ( for (vector <- vectorsMap)
      yield {
        dbidVertexIDMapping.put(i.toString, vector._1)
        i+=1
        vector._2.mkString(";") //+ ";" + vector._1.id
      }) mkString "\n"
    )
    out.close()

    dbidVertexIDMapping
  }


  def getDBIDRefFromVertex(relation: Relation[DoubleVector], vertex: SVertex, dbidVertexIDMapping: MutableHashMap[String, SVertex] ): DBIDRef = {
    val dbid = Integer.parseInt(dbidVertexIDMapping.find(_._2 == vertex).get._1)
    val iter = relation.iterDBIDs
    while (iter.valid) {
      if (iter.internalGetIndex == dbid)
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