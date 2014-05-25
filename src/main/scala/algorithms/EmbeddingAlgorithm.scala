package algorithms

import scala.util.Random
import scala.collection.mutable.HashMap

import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction
import de.lmu.ifi.dbs.elki.distance.distancevalue.{DoubleDistance}
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction
import de.lmu.ifi.dbs.elki.database.ids.distance.DistanceDBIDList
import de.lmu.ifi.dbs.elki.database.ids.{DBID, DBIDUtil}
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
import java.nio.file.{Files, Paths}
import java.io.{BufferedWriter, FileWriter}


/**
 * @author fliebhart
 */
object EmbeddingAlgorithm {

  def embeddedRKNNs(sGraph: SGraph, sQ: SVertex, k: Int, numRefPoints: Integer): IndexedSeq[(DBID, Double)] = {
    val rTreePath  = "tplSimulation/rTree.csv"

//    Utils.generateCSVFile(numRefPoints, 100, rTreePath) // dimensions = numRefPoints, number of random vectors to be created = 100

    val refPoints        : Seq[SVertex]                         = createRefPoints(vertices = sGraph.getAllVertices, numRefPoints)
    val refPointDistances: HashMap[SVertex, IndexedSeq[Double]] = createEmbedding(sGraph, refPoints) // key: Knoten, value: Liste mit Distanzen zu Referenzpunkte (? evtl: Flag ob Objekt auf Knoten)
    writeRTreeCSVFile(refPointDistances, rTreePath)

    // create Memory Database
    val db: List[Database] = Utils.createDatabase(rTreePath).toList
    val relation: Relation[DoubleVector] = db(0).getRelation(TypeUtil.NUMBER_VECTOR_FIELD)

    // create RStar Tree
    val rStarTreePageSize = 1024  // Determines how many points fit into one page. Felix: Mostly between 1024 und 8192 byte. 1024 byte correspond to about 22 points
    val dbIndex           = Utils.createRStarTree(relation, rStarTreePageSize)
    val distanceFunction  = EuclideanDistanceFunction.STATIC
    val distanceQuery     = distanceFunction.instantiate(relation)
    val gtpl = new GenericTPLRkNNQuery[RStarTreeNode, SpatialEntry, DoubleVector, DoubleDistance](dbIndex, distanceQuery, false) // withClipping = false

    // Generate random query point
//    val coordinates: Array[Double] = new Array[Double](dimensions)
//    for (i <- 0 to dimensions) {
//      coordinates(i) = Math.random()
//    }
//    val queryObject: DoubleVector = new DoubleVector(coordinates);
//    println("Generated query object: " + queryObject)

    // random query object from the database
    val queryObject: DoubleVector = relation.get(Utils.getRandomDBObject(relation))
    println(s"Random query object from database: $queryObject\n")

    // Performing RkNN query
    println("Performing RkNN-query...")
    val t0 = System.currentTimeMillis()
    val distanceDBIDList: DistanceDBIDList[DoubleDistance] = gtpl.getRKNNForObject(queryObject, k)
    val t1 = System.currentTimeMillis()
    println(s"RkNN query performed in ${t1-t0} ms.\n")


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
  def createEmbedding(sGraph: SGraph, refPoints: Seq[SVertex]): HashMap[SVertex, IndexedSeq[Double]] = {
    //  // pure functional, immutable implementation: (maps still need to be merged)
    //  import scala.collection.immutable.HashMap
    //  val allRefPointDistances = refPoints.map(refPoint => Dijkstra.dijkstra(sGraph, refPoint))
    //  allRefPointDistances.map(_.foldLeft(new HashMap[SVertex, Seq[Double]]) ((x, y) => x + (y._1 -> x.get(y._1).getOrElse(Nil).:+(y._2))  ))
    val refpointDistances = HashMap[SVertex, IndexedSeq[Double]]()

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
  def writeRTreeCSVFile(vectorsMap: HashMap[SVertex, IndexedSeq[Double]], destPath: String) {
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


  /**
   * Berechnet max. minimale Distanz
   *
   */
  def minDist(p1: SVertex, p2: SVertex){

  }

  /**
   * Berechnet "minimale maximale" Distanz
   *
   */
  def maxDist(p1: SVertex, p2: SVertex){

  }

  def getReverseKNearestNeighbours(graph: SGraph, q: SVertex, k: Int): IndexedSeq[VD] = {
    var RkNN_q         = IndexedSeq.empty[VD]

    RkNN_q.sortWith((x,y) => (x._2 < y._2) || (x._2 == y._2) && (x._1.id < y._1.id))
  }
}