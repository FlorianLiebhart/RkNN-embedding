package algorithms

import graph.core.{Graph, Vertex}
import scala.collection.JavaConversions._
import util.Utils.VD
import scala.collection.mutable.PriorityQueue
import util.Utils.t2ToOrdered

object NaiveRkNN {

  def getReverseKNearestNeighbors(graph: Graph, q: Vertex, k: Integer): IndexedSeq[VD] = {
    val allGraphNodes = graph.getAllVertices.toIndexedSeq
    val allGraphNodesWithObjects = allGraphNodes filter (_.containsObject()) filterNot (_ equals q)
    val allkNNs = allGraphNodesWithObjects map ( p => (p, Eager.rangeNN(graph, p, k, Double.PositiveInfinity, false)))

    val rKnns = allkNNs collect {
      case (v, knns) if knns map( _._1 ) contains q => new VD(v, knns.find(y => (y._1 equals q)).get._2)
    }

//    val rKnns = allkNNs filter (x => x._2 map (_._1) contains q)
//    val rKnnsFilteredDists = rKnns map (x => new VD (x._1, x._2.find(_._1 equals q).get._2))

    val h = new PriorityQueue[VD]()
    rKnns.map(x => h.enqueue(x))
    h.toIndexedSeq
  }
}