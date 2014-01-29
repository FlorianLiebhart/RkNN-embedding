package util

import graph.core.{Edge, Vertex, Graph}

/**
 * Created: 28.01.14, 02:22
 * @author fliebhart
 */
object Utils {


  type VD = Tuple2[Vertex, Double] // (Vertex, Distance from q)
  implicit def t2ToOrdered(thisT2: VD): Ordered[VD] = new Ordered[VD] {
      def compare(otherT2: VD): Int = otherT2._2.compare(thisT2._2)
  }

  /**
   * @return A New example Graph from Figure 3 of paper TKDE - GraphRNN
   */
  def createExampleGraph: Graph = {
    val n1 = new Vertex(1)
    val n2 = new Vertex(2)
    val n3 = new Vertex(3)
    val n4 = new Vertex(4)
    n4.setObjectId(0)       // query point
    val n5 = new Vertex(5)
    n5.setObjectId(2)
    val n6 = new Vertex(6)
    n6.setObjectId(1)
    val n7 = new Vertex(7)
    n7.setObjectId(3)

    val graph: Graph = new Graph()
    graph.addVertex(n1)
    graph.addVertex(n2)
    graph.addVertex(n3)
    graph.addVertex(n4)
    graph.addVertex(n5)
    graph.addVertex(n6)
    graph.addVertex(n7)

    val e41 = new Edge(n4, n1)
    e41.setWeight(5)
    graph.addEdge(e41)

    val e17 = new Edge(n1, n7)
    e17.setWeight(6)
    graph.addEdge(e17)

    val e72 = new Edge(n7, n2)
    e72.setWeight(6)
    graph.addEdge(e72)

    val e26 = new Edge(n2, n6)
    e26.setWeight(5)
    graph.addEdge(e26)

    val e63 = new Edge(n6, n3)
    e63.setWeight(3)
    graph.addEdge(e63)

    val e34 = new Edge(n3, n4)
    e34.setWeight(4)
    graph.addEdge(e34)

    val e15 = new Edge(n1, n5)
    e15.setWeight(3)
    graph.addEdge(e15)

    val e53 = new Edge(n5, n3)
    e53.setWeight(6)
    graph.addEdge(e53)

    val e52 = new Edge(n5, n2)
    e52.setWeight(4)
    graph.addEdge(e52)

    graph
  }
}
