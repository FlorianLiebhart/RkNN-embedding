package algorithms

import graph.{SVertex, SGraph}
import scala.collection.immutable.HashMap
import util.Utils._

/**
 * Created: 04.02.14, 11:47
 * @author fliebhart
 */
object EmbeddingAlgorithm {

  // key: Knotwn, value: Liste mit Distanzen zu Referenzpunkte (? evtl: Flag ob Objekt auf Knoten)
  val refpointDistances = HashMap[SVertex, IndexedSeq[Double]]

//  def embeddingAlgorithm (numberOfRefPoints: Integer)



  /**
   * Takes a graph and creates numberOfRefPoints random reference points.
   * @param graph
   * @param numberOfRefPoints
   */
  def createRefPoints(graph: SGraph, numberOfRefPoints: Integer){

  }

  def getReverseKNearestNeighbours(graph: SGraph, q: SVertex, k: Int): IndexedSeq[VD] = {
    var RkNN_q         = IndexedSeq.empty[VD]

    RkNN_q.sortWith((x,y) => (x._2 < y._2) || (x._2 == y._2) && (x._1.id < y._1.id))
  }
}
