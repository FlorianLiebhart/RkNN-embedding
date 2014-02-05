package graph

import java.awt.Point
import SVertex._

/**
 * Created: 31.12.13, 13:43
 * @author fliebhart
 */

object SVertex {
  val NO_OBJECT = -1
}

class SVertex(val id: Int) {

  var objectId: Int = NO_OBJECT
  private var pos: Point = null

  override def toString: String = this.id + ""

  def setNodeLocation(x: Int, y: Int) {
    pos = new Point(x, y)
  }

  def setObjectId(id: Int) {
    objectId = id
  }

  def getObjectId: Int = {
    return objectId
  }

  def getNodeLocation: Point = pos

  def containsObject: Boolean = objectId != SVertex.NO_OBJECT
}
