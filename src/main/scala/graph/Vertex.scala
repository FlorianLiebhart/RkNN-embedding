package graph

import java.awt.Point
import Vertex._

/**
 * Created: 31.12.13, 13:43
 * @author fliebhart
 */

object Vertex {
  val NO_OBJECT = -1
}

class Vertex(var id: Int) {

  var objectId: Int = NO_OBJECT
  private var pos: Point = _

  override def toString: String = this.id + ""

  def setNodeLocation(x: Int, y: Int) {
    pos = new Point(x, y)
  }

  def getNodeLocation: Point = pos

  def containsObject: Boolean = objectId != Vertex.NO_OBJECT
}
