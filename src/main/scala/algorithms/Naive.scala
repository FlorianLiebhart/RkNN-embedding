package algorithms

import graph.{SGraph, SVertex}
import util.Utils.{ThreadCPUTimeDiff, VD}
import util.Log

case object Naive extends GraphRknn{

  override val name = "Naive"
  /**
   *
   * @param sGraph
   * @param q
   * @param k
   * @return Sorted rknns
   */
  def rknns(sGraph: SGraph, q: SVertex, k: Int): Seq[VD] = {
    val timeTotalRknn = ThreadCPUTimeDiff()

    val allGraphNodes            = sGraph.getAllVertices.toIndexedSeq
    val allGraphNodesWithObjects = allGraphNodes filter (_.containsObject) filterNot (_ equals q)
    Log.nodesToVerify = allGraphNodesWithObjects.size

    // If q doesn't contain an object, give it an object so that it will be found by the knn algorithm
    val qContainsObject = q.containsObject
    if (!qContainsObject) q.setObjectId(sGraph.getAllVertices.size)

    val allkNNs = allGraphNodesWithObjects map ( p =>
      (p, Eager.rangeNN(sGraph, p, k, Double.PositiveInfinity))
    )
    // If q didn't contain an object before, remove the previously inserted object
    if (!qContainsObject)
      q.setObjectId(SVertex.NO_OBJECT)

    val rKnns = allkNNs collect {
      case (v, knns) if knns map( _._1 ) contains q => new VD(v, knns.find(y => (y._1 equals q)).get._2)
    }

//    val rKnns = allkNNs filter (x => x._2 map (_._1) contains q)
//    val rKnnsFilteredDists = rKnns map (x => new VD (x._1, x._2.find(_._1 equals q).get._2))

    timeTotalRknn.end
    Log.runTimeRknnQuery = timeTotalRknn.diffMillis

    rKnns.sortWith((x,y) => (x._2 < y._2) || (x._2 == y._2) && (x._1.id < y._1.id))
  }
}