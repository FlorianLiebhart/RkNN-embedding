package graph

/**
 * Created: 31.12.13, 13:29
 * @author fliebhart
 */
class Edge(val source: Vertex, val target: Vertex) {
  var weight: Double = _

  override def toString: String = "(" + source + " - " + target + ")"

  def containsVertex(vertex: Vertex): Boolean = vertex == source || vertex == target
}
