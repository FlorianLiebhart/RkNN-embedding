package algorithms

import graph.{SVertex, SGraph}
import scala.collection.mutable.HashMap
import _root_.util.Utils._
import scala.util.Random
import scala._

/**
 * Created: 04.02.14, 11:47
 * @author fliebhart
 */
object EmbeddingAlgorithm {

  var refPoints: Seq[SVertex] = IndexedSeq[SVertex]()

  // key: Knoten, value: Liste mit Distanzen zu Referenzpunkte (? evtl: Flag ob Objekt auf Knoten)
  var refpointDistances = HashMap[SVertex, IndexedSeq[Double]]()


  def start(sGraph: SGraph, numRefPoints: Integer){
    refPoints = createRefPoints(sGraph.getAllVertices, numRefPoints)
    refpointDistances = createEmbedding(sGraph, refPoints)
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
      allDistsFromRefPoint map (x => refpointDistances.put(x._1, (refpointDistances.get(x._1).getOrElse(Nil) :+ x._2).toIndexedSeq)) // evtl. finetuning bei "toIndexedSeq" -> Später, wenns läuft, mit Seq probieren!
    }
    refpointDistances
  }

  /**
   * Berechnet minimale Distanz
   *
   */
  def minDist(p1: SVertex, p2: SVertex){

  }

  /**
   * Berechnet "minimale maximale" Distanz
   * (
   */
  def maxDist(p1: SVertex, p2: SVertex){

  }

  def getReverseKNearestNeighbours(graph: SGraph, q: SVertex, k: Int): IndexedSeq[VD] = {
    var RkNN_q         = IndexedSeq.empty[VD]

    RkNN_q.sortWith((x,y) => (x._2 < y._2) || (x._2 == y._2) && (x._1.id < y._1.id))
  }
}