package algorithms

import scala.util.Random
import scala.collection.mutable.LinkedHashMap

import java.nio.file.{Files, Paths}
import java.io.{BufferedWriter, FileWriter}
import java.lang.IllegalArgumentException

import de.lmu.ifi.dbs.elki.distance.distancevalue.{DoubleDistance}
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction
import de.lmu.ifi.dbs.elki.database.ids.distance.DistanceDBIDList
import de.lmu.ifi.dbs.elki.database.ids.{DBIDIter, DBID, DBIDUtil}
import de.lmu.ifi.dbs.elki.database.Database
import de.lmu.ifi.dbs.elki.database.relation.Relation
import de.lmu.ifi.dbs.elki.data.{DoubleVector}
import de.lmu.ifi.dbs.elki.data.`type`.TypeUtil
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar._
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialEntry
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.strategies.bulk.SortTileRecursiveBulkSplit

import elkiTPL.{Utils, GenericTPLRkNNQuery}
import graph.{SVertex, SGraph}
import util.Utils._

/**
 * @author fliebhart
 */
object EmbeddingAlgorithm {

  /**
   *
   * @param sGraph
   * @param sQ
   * @param k
   * @param refPoints
   * @param rStarTreePageSize Determines how many points fit into one page. Felix: Mostly between 1024 und 8192 byte. 1024 byte correspond to about 22 points
   * @return
   */
  def embeddedRKNNs(sGraph: SGraph, sQ: SVertex, k: Int, refPoints: Seq[SVertex], rStarTreePageSize: Int): IndexedSeq[(DBID, Double)] = {
    val rTreePath  = "tplSimulation/rTree.csv"
    println(s"Reference Points' IDs: ${refPoints.mkString(",")}")

    // create RTree CSV File
    // Utils.generateCSVFile(refPoints.size, 100, rTreePath) // dimensions = numRefPoints, number of random vectors to be created = 100

    val refPointDistances: LinkedHashMap[SVertex, IndexedSeq[Double]] = createEmbedding(sGraph, refPoints) // key: Knoten, value: Liste mit Distanzen zu Referenzpunkte (? evtl: Flag ob Objekt auf Knoten)
    writeRTreeCSVFile(refPointDistances, rTreePath)

    // create memory database
    val db: List[Database]               = Utils.createDatabase(rTreePath).toList
    val relation: Relation[DoubleVector] = db(0).getRelation(TypeUtil.NUMBER_VECTOR_FIELD)

    // create RStar tree
    val rStarTree         = Utils.createRStarTree(relation, rStarTreePageSize)
    val distanceFunction  = EuclideanDistanceFunction.STATIC
    val distanceQuery     = distanceFunction.instantiate(relation)
    val gTPL              = new GenericTPLRkNNQuery[RStarTreeNode, SpatialEntry, DoubleVector, DoubleDistance](rStarTree, distanceQuery, false) // withClipping = false
/*
    // Generate random query point
    val coordinates: Array[Double] = new Array[Double](refPoints.size)
      for (i <- 0 to refPoints.size-1) {
      coordinates(i) = Math.random()
    }
    val queryObject: DoubleVector = new DoubleVector(coordinates)
    println("Generated query object: " + queryObject)

    // random query object from the database
    val queryObject: DoubleVector = relation.get(Utils.getRandomDBObject(relation))
    println(s"Random query object from database: $queryObject\n")
*/
    val queryObject: DoubleVector = relation.get(getDBIDFromVertexId(relation.getDBIDs.iter, sQ.id))


    // Performing RkNN query
    println(s"Performing R${k}NN-query...")
    val t0 = System.currentTimeMillis()
    val distanceDBIDList: DistanceDBIDList[DoubleDistance] = gTPL.getRKNNForObject(queryObject, k)
    val t1 = System.currentTimeMillis()
    println(s"R${k}NN query performed in ${t1-t0} ms.\n")


    var rkNNs: IndexedSeq[(DBID, Double)] = IndexedSeq.empty
    val rkNNIter = distanceDBIDList.iter()
    while (rkNNIter.valid()) {
      rkNNs :+= (DBIDUtil.deref(rkNNIter), rkNNIter.getDistance.doubleValue)
      rkNNIter.advance()
    }
    rkNNs
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
  def createEmbedding(sGraph: SGraph, refPoints: Seq[SVertex]): LinkedHashMap[SVertex, IndexedSeq[Double]] = {
    //  // pure functional, immutable implementation: (maps still need to be merged)
    //  import scala.collection.immutable.HashMap
    //  val allRefPointDistances = refPoints.map(refPoint => Dijkstra.dijkstra(sGraph, refPoint))
    //  allRefPointDistances.map(_.foldLeft(new HashMap[SVertex, Seq[Double]]) ((x, y) => x + (y._1 -> x.get(y._1).getOrElse(Nil).:+(y._2))  ))
    val refpointDistances = LinkedHashMap[SVertex, IndexedSeq[Double]]()

    for(refPoint <- refPoints){
      val allDistsFromRefPoint = Dijkstra.dijkstra(sGraph, refPoint)
      allDistsFromRefPoint map { x =>
        refpointDistances.put(
          x._1,
          (refpointDistances.get(x._1).getOrElse(Nil) :+ x._2).toIndexedSeq
        )
      } // evtl. finetuning bei "toIndexedSeq" -> Später, wenns läuft, mit Seq probieren!
    }
    refpointDistances
  }


  /**
   * Takes a map of Vertex -> Distances and writes it as a CSV file to the given path as an RTree
   * @param vectorsMap
   * @param destPath
   * @return
   */
  def writeRTreeCSVFile(vectorsMap: LinkedHashMap[SVertex, IndexedSeq[Double]], destPath: String) {
    // create directories and file if non-existent
    val pathToFile = Paths.get(destPath)
    Files.createDirectories(pathToFile.getParent())
    if (!Files.exists(pathToFile))
        Files.createFile(pathToFile)
    val fw  = new FileWriter(destPath, false) // false = overwrite current file content
    val out = new BufferedWriter(fw)

//    val doubleUKformatter: NumberFormat = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.UK))
//    newline += doubleUKformatter.format(Math.random()) + ";"

    out.write(
      vectorsMap.map( vector =>
        vector._2 mkString ";"
      ) mkString "\n"
    )
    out.close()
  }


  // Not proper! DBID should not be used as external ID, according to ELKI
  def getDBIDFromVertexId(iter: DBIDIter, id: Int): DBID = {
    while (iter.valid) {
      if (DBIDUtil.asInteger(iter) == id + 1)
        return DBIDUtil.deref(iter)
      iter.advance()
    }
    throw new IllegalArgumentException("Iter not valid or id not found.")
  }

  // Not proper! DBID should not be used as external ID, according to ELKI
  def getVertexFromDBID(dbid: DBID, refPointDistances: LinkedHashMap[SVertex, IndexedSeq[Double]]): SVertex = {
    return refPointDistances.keys.toIndexedSeq(DBIDUtil.asInteger(dbid) - 1)
  }



  /*
  /**
   * Berechnet max. minimale Distanz
   */
  def minDist(p1: SVertex, p2: SVertex){
  }

  /**
   * Berechnet "minimale maximale" Distanz
   */
  def maxDist(p1: SVertex, p2: SVertex){
  }

  def getReverseKNearestNeighbours(graph: SGraph, q: SVertex, k: Int): IndexedSeq[VD] = {
    var RkNN_q         = IndexedSeq.empty[VD]
    RkNN_q.sortWith((x,y) => (x._2 < y._2) || (x._2 == y._2) && (x._1.id < y._1.id))
  }
  */
}