package util

import graph.SVertex

object Utils {

  /**
   * Throws IllegalArgumentException if b is false
   * @param b
   */
  def makesure(b: Boolean, errMsg: String) = if (!b) throw new IllegalArgumentException(errMsg)

  type VD = Tuple2[SVertex, Double] // (Vertex, Distance from q)
  implicit def t2ToOrdered(thisT2: VD): Ordered[VD] = new Ordered[VD] {
      def compare(otherT2: VD): Int = otherT2._2.compare(thisT2._2)
  }
}
