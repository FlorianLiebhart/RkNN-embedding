package algorithms

import graph.{SVertex, SGraph}
import scala.collection.mutable.HashMap
import _root_.util.Utils._
import scala.util.Random
import scala._
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants._
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar._
import de.lmu.ifi.dbs.elki.persistent._
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.strategies.bulk.SortTileRecursiveBulkSplit
import elkiTPL.{Simulation, GenericTPLRkNNQuery}
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction
import de.lmu.ifi.dbs.elki.distance.distancevalue.DoubleDistance
import de.lmu.ifi.dbs.elki.database.ids.distance.DistanceDBIDList
import de.lmu.ifi.dbs.elki.database.ids.{DBID, DBIDUtil, DBIDIter}

/**
 * @author fliebhart
 */
object EmbeddingAlgorithm {


  def embeddedRKNNs(sGraph: SGraph, sQ: SVertex, k: Int, numRefPoints: Integer): IndexedSeq[(DBID, Double)] = {
    val refPoints: Seq[SVertex] = createRefPoints(sGraph.getAllVertices, numRefPoints)
    // key: Knoten, value: Liste mit Distanzen zu Referenzpunkte (? evtl: Flag ob Objekt auf Knoten)
    val refpointDistances: HashMap[SVertex, IndexedSeq[Double]] = createEmbedding(sGraph, refPoints)

    val rTreePath = "tplSimulation/rTree.csv"
    val dimensions = numRefPoints
    val numPoints = 100
    val tplSimulation = new Simulation()
    tplSimulation.generateCSVFile(dimensions, numPoints, rTreePath)

    val pageSize = 1024  // Determines how many points fit into one page. Felix: Mostly between 1024 und 8192 byte. 1024 byte correspond to about 22 points
    val distanceDBIDList: DistanceDBIDList[DoubleDistance] = tplSimulation.simulate(rTreePath, pageSize, k, dimensions, true)

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
    //    // pure functional, immutable implementation: (maps still need to be merged)
    //    import scala.collection.immutable.HashMap
    //    val allRefPointDistances = refPoints.map(refPoint => Dijkstra.dijkstra(sGraph, refPoint))
    //    allRefPointDistances.map(_.foldLeft(new HashMap[SVertex, Seq[Double]]) ((x, y) => x + (y._1 -> x.get(y._1).getOrElse(Nil).:+(y._2))  ))
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