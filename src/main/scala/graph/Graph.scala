package graph

import java.util.Random
import scala.collection.mutable.{ HashSet, HashMap }

/**
 * Created: 31.12.13, 13:35
 * @author fliebhart
 */
class Graph {

  private var vertices: HashMap[Int, Vertex] = new HashMap[Int, Vertex]()
  private var edges: HashSet[Edge] = new HashSet[Edge]()
  private var mapping: HashMap[Vertex, HashMap[Vertex, Edge]] = new HashMap[Vertex, HashMap[Vertex, Edge]]()

  def addVertex(v: Vertex) {
    this.vertices += v.id -> v
    this.mapping += v -> new HashMap[Vertex, Edge].empty
  }

  def addVertex(): Vertex = {
    var id = 0
    while (getVertex(id) != null)
      id += 1

    val v = new Vertex(id)
    this.vertices += v.id -> v
    this.mapping += v -> new HashMap[Vertex, Edge].empty
    v
  }

  def getVertex(nodeId: Int): Vertex = vertices.getOrElse(nodeId, null)

  def removeVertex(v: Vertex) {
    this.vertices -= v.id
    val mapping = this.mapping.getOrElse(v, Map.empty)

    for (e <- mapping.values if e.source == v || e.target == v) {
      this.edges -= e
      this.mapping.getOrElse(e.source, HashMap.empty) -= e.target
      this.mapping.getOrElse(e.target, HashMap.empty) -= e.source
    }
  }

  def setVertices(vertices: Vector[Vertex]) {
    this.vertices = HashMap.empty[Int, Vertex]
    this.mapping = HashMap.empty[Vertex, HashMap[Vertex, Edge]]
    for (v <- vertices) {
      this.vertices += v.id -> v
      this.mapping += v -> new HashMap[Vertex, Edge].empty
    }
  }

  def addEdge(e: Edge) {
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

  def removeEdge(e: Edge) {
    this.edges -= e
    this.mapping.getOrElse(e.source, HashMap.empty) -= e.target
    this.mapping.getOrElse(e.target, HashMap.empty) -= e.source
  }

  def setEdges(edges: Vector[Edge]) {
    this.edges = new HashSet[Edge]()
    this.mapping = new HashMap[Vertex, HashMap[Vertex, Edge]]()
    for (v <- this.vertices.values) {
      this.mapping += v -> new HashMap[Vertex, Edge].empty
    }
    for (e <- edges) {
      this.addEdge(e)
    }
  }

  def getEdge(v1: Int, v2: Int): Edge = {
    val start = getVertex(v1)
    val end = getVertex(v2)
    if (start == null || end == null) {
      return null
    }
    mapping.get(start).get(end)
  }

  def getEdge(start: Vertex, end: Vertex): Edge = {
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

  def containsEdge(edge: Edge): Boolean = {
    val start = edge.source
    val end = edge.target
    if (start == null || end == null) {
      return false
    }
    mapping.get(start).get(end) != null
  }

  def getNeighborsFrom(home: Vertex): Vector[Vertex] = {
    val neighbors = Vector.empty[Vertex]
    val mapping = this.mapping.getOrElse(home, Map.empty)
    for (e <- mapping.values) {
      if (e.source == home)
        neighbors :+ e.target

      if (e.target == home)
        neighbors :+ e.source
    }
    neighbors
  }

  def getEdgesFrom(home: Vertex): Iterable[Edge] = this.mapping.getOrElse(home, Map.empty).values

  def getPredecessorFrom(home: Vertex): Iterable[Vertex] = {
    val mapping = this.mapping.getOrElse(home, Map.empty)
    mapping.values.filter(_.target == home).map(_.source)
  }

  def getSuccessorFrom(home: Vertex): Iterable[Vertex] = {
    val mapping = this.mapping.getOrElse(home, Map.empty)
    mapping.values.filter(_.source == home).map(_.target)
  }

  def equals(graph: Graph): Boolean = {
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

  def equalsIgnoreEdges(graph: Graph): Boolean = {
    if (this.vertices.size != graph.getNumberOfVertices)
      return false

    for (v <- graph.getAllVertices if !this.vertices.values.toSeq.contains(v))
      return false

    true
  }

  def getNumberOfNodesWithObjects: Int = this.vertices.values.count(_.containsObject)

  def getAllEdges: Seq[Edge] = this.edges.toSeq

  def getAllVertices: Seq[Vertex] = this.vertices.values.toSeq

  def getNumberOfVertices: Int = vertices.size

  def getNumberOfEdges: Int = edges.size

  def setAllWeightWithLimit(upperLimit: Int) {
    val randomer = new Random()
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
