package graph

import java.util.Random
import scala.collection.mutable.{ HashSet, HashMap }

/**
 * Created: 31.12.13, 13:35
 * @author fliebhart
 */
class SGraph {

  private var vertices: HashMap[Int, SVertex] = new HashMap[Int, SVertex]()
  private var edges: HashSet[SEdge] = new HashSet[SEdge]()
  private var mapping: HashMap[SVertex, HashMap[SVertex, SEdge]] = new HashMap[SVertex, HashMap[SVertex, SEdge]]()

  def addVertex(v: SVertex) {
    this.vertices += v.id -> v
    this.mapping += v -> new HashMap[SVertex, SEdge].empty
  }

  def addVertex(): SVertex = {
    var id = 0
    while (getVertex(id) != null)
      id += 1

    val v = new SVertex(id)
    this.vertices += v.id -> v
    this.mapping += v -> new HashMap[SVertex, SEdge].empty
    v
  }

  def getVertex(nodeId: Int): SVertex = vertices.getOrElse(nodeId, null)

  def removeVertex(v: SVertex) {
    this.vertices -= v.id
    val mapping = this.mapping.getOrElse(v, Map.empty)

    for (e <- mapping.values if e.source == v || e.target == v) {
      this.edges -= e
      this.mapping.getOrElse(e.source, HashMap.empty) -= e.target
      this.mapping.getOrElse(e.target, HashMap.empty) -= e.source
    }
  }

  def setVertices(vertices: Vector[SVertex]) {
    this.vertices = HashMap.empty[Int, SVertex]
    this.mapping = HashMap.empty[SVertex, HashMap[SVertex, SEdge]]
    for (v <- vertices) {
      this.vertices += v.id -> v
      this.mapping += v -> new HashMap[SVertex, SEdge].empty
    }
  }

  def addEdge(e: SEdge) {
    this.edges += e
    try {
      val m1 = this.mapping.get(e.source)
      m1.getOrElse(null)
      this.mapping.getOrElse(e.source, HashMap.empty) += e.target -> e
      this.mapping.getOrElse(e.target, HashMap.empty) += e.source -> e
    } catch {
      case ex: NullPointerException => println("Error adding edge " + e.toString + ": " + ex.getLocalizedMessage)
    }
  }

  def removeEdge(e: SEdge) {
    this.edges -= e
    this.mapping.getOrElse(e.source, HashMap.empty) -= e.target
    this.mapping.getOrElse(e.target, HashMap.empty) -= e.source
  }

  def setEdges(edges: Vector[SEdge]) {
    this.edges = new HashSet[SEdge]()
    this.mapping = new HashMap[SVertex, HashMap[SVertex, SEdge]]()
    for (v <- this.vertices.values) {
      this.mapping += v -> new HashMap[SVertex, SEdge].empty
    }
    for (e <- edges) {
      this.addEdge(e)
    }
  }

  def getEdge(v1: Int, v2: Int): SEdge = {
    val start = getVertex(v1)
    val end = getVertex(v2)
    if (start == null || end == null) {
      return null
    }
    mapping.get(start).get(end)
  }

  def getEdge(start: SVertex, end: SVertex): SEdge = {
    if (start == null || end == null) {
      return null
    }
    mapping.get(start).get(end)
  }

  def containsEdge(v1: Int, v2: Int): Boolean = {
    val start = getVertex(v1)
    val end = getVertex(v2)
    if (start == null || end == null) {
      return false
    }
    mapping.get(start).get(end) != null
  }

  def containsEdge(edge: SEdge): Boolean = {
    val start = edge.source
    val end = edge.target
    if (start == null || end == null) {
      return false
    }
    mapping.get(start).get(end) != null
  }

  def getNeighborsFrom(home: SVertex): Vector[SVertex] = {
    var neighbors = Vector.empty[SVertex]
    var mapping = this.mapping.getOrElse(home, Map.empty)
    for (e <- mapping.values) {
      if (e.source == home)
        neighbors :+= e.target

      if (e.target == home)
        neighbors :+= e.source
    }
    neighbors
  }

  def getEdgesFrom(home: SVertex): Iterable[SEdge] = this.mapping.getOrElse(home, Map.empty).values

  def getPredecessorFrom(home: SVertex): Iterable[SVertex] = {
    val mapping = this.mapping.getOrElse(home, Map.empty)
    mapping.values.filter(_.target == home).map(_.source)
  }

  def getSuccessorFrom(home: SVertex): Iterable[SVertex] = {
    val mapping = this.mapping.getOrElse(home, Map.empty)
    mapping.values.filter(_.source == home).map(_.target)
  }

  def equals(graph: SGraph): Boolean = {
    if (this.vertices.size != graph.getNumberOfVertices) {
      return false
    }
    if (this.edges.size != graph.getNumberOfEdges) {
      return false
    }
    for (e <- edges if !graph.containsEdge(e.source.id, e.target.id)) {
      return false
    }
    true
  }

  def equalsIgnoreEdges(graph: SGraph): Boolean = {
    if (this.vertices.size != graph.getNumberOfVertices)
      return false

    for (v <- graph.getAllVertices if !this.vertices.values.toIndexedSeq.contains(v))
      return false

    true
  }

  def getNumberOfNodesWithObjects: Int = this.vertices.values.count(_.containsObject)

  def getAllEdges: Seq[SEdge] = this.edges.toIndexedSeq

  def getAllVertices: Seq[SVertex] = this.vertices.values.toIndexedSeq

  def getNumberOfVertices: Int = vertices.size

  def getNumberOfEdges: Int = edges.size

  def setAllWeightWithLimit(upperLimit: Int) {
    val randomer = new Random(System.currentTimeMillis())
    for (e <- edges) {
      var w = randomer.nextInt(upperLimit + 1)
      while (w == 0) {
        w = randomer.nextInt(upperLimit + 1)
      }
      e.weight = w
    }
  }

  def setAllWeight(weight: Double) {
    for (e <- edges) {
      e.weight = weight
    }
  }

  def generateObjectId(fromValue: Int): Int = {
    var id = fromValue
    while (this.containsObject(id)) {
      id += 1
    }
    id
  }

  def containsObject(objectId: Int): Boolean = {
    this.getAllVertices
      .find(_.id == objectId).exists(_ => true)
  }
}
