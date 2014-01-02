import algorithms.Eager
import graph.core.{Edge, Graph, Vertex}

object RkNNComparator {

  def main(args: Array[String]): Unit = {
    /*
         * 1.) Initialize a large graph and generate many nodes and objects.
         *
         * 2.) Code-Organization Phase
         * a)  Perform eager algorithm on some random nodes of the graph.
         * b)  Perform competing algorithm on some random nodes of the graph.
         *
         * 3.) RkNN performance measuring & comparison
         * a) Perform RkNN-search with the eager algorithm, measure runtime.
         * b) Perform RkNN-search with competing algorithm, measure runtime.
         *
         * 4.) Print out runtime difference
    */

    val graph = createExampleGraph

    for( v: Vertex <- Eager.eager(graph, graph.getVertex(4), 1))
      println(v.getId)

  }

  /**
   * @return A New example Graph from Figure 3 of paper TKDE - GraphRNN
   */
  def createExampleGraph: Graph = {
    val n1 = new Vertex(1)
    val n2 = new Vertex(2)
    val n3 = new Vertex(3)
    val n4 = new Vertex(4)
//    n4.setObjectId(0)
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
    val e14 = new Edge(n1, n4)
    e41.setWeight(5)
    e14.setWeight(5)
    graph.addEdge(e41)
    graph.addEdge(e14)

    val e17 = new Edge(n1, n7)
    val e71 = new Edge(n7, n1)
    e17.setWeight(6)
    e71.setWeight(6)
    graph.addEdge(e17)
    graph.addEdge(e71)

    val e72 = new Edge(n7, n2)
    val e27 = new Edge(n2, n7)
    e72.setWeight(6)
    e27.setWeight(6)
    graph.addEdge(e72)
    graph.addEdge(e27)

    val e26 = new Edge(n2, n6)
    val e62 = new Edge(n6, n2)
    e26.setWeight(5)
    e62.setWeight(5)
    graph.addEdge(e26)
    graph.addEdge(e62)

    val e63 = new Edge(n6, n3)
    val e36 = new Edge(n3, n6)
    e63.setWeight(3)
    e36.setWeight(3)
    graph.addEdge(e63)
    graph.addEdge(e36)

    val e34 = new Edge(n3, n4)
    val e43 = new Edge(n4, n3)
    e34.setWeight(4)
    e43.setWeight(4)
    graph.addEdge(e34)
    graph.addEdge(e43)

    val e15 = new Edge(n1, n5)
    val e51 = new Edge(n5, n1)
    e15.setWeight(3)
    e51.setWeight(3)
    graph.addEdge(e15)
    graph.addEdge(e51)

    val e53 = new Edge(n5, n3)
    val e35 = new Edge(n3, n5)
    e53.setWeight(6)
    e35.setWeight(6)
    graph.addEdge(e53)
    graph.addEdge(e35)

    val e52 = new Edge(n5, n2)
    val e25 = new Edge(n2, n5)
    e52.setWeight(4)
    e25.setWeight(4)
    graph.addEdge(e52)
    graph.addEdge(e25)

    graph
  }

}