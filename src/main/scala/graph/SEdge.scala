package graph

/**
 * Created: 31.12.13, 13:29
 * @author fliebhart
 */
class SEdge(val source: SVertex, val target: SVertex) {
  var weight: Double = _

  def containsVertex(vertex: SVertex): Boolean = vertex == source || vertex == target

  def getWeight: Double = {
    return weight
  }

  def setWeight(weight: Double) {
    this.weight = weight
  }

  override def toString: String = "(" + source + " - " + target + ")"
}
