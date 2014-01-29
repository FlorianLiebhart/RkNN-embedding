package app

import algorithms.Eager
import graph.core.{Edge, Graph, Vertex}
import util.Utils

object RkNNComparator {

  def main(args: Array[String]): Unit = {
    /*
     *  1.) Initialize a large graph and generate many nodes and objects.
     *
     *  2.) Code-Organization Phase
     *  a)  Perform eager algorithm on some random nodes of the graph.
     *  b)  Perform competing algorithm on some random nodes of the graph.
     *
     *  3.) RkNN performance measuring & comparison
     *  a) Perform RkNN-search with the eager algorithm, measure runtime.
     *  b) Perform RkNN-search with competing algorithm, measure runtime.
     *
     *  4.) Print out runtime difference
     */

    val graph = Utils.createExampleGraph
    val rKnns = Eager.eager(graph, graph.getVertex(4), 1)
    for( v <- rKnns )
      println("Node: " + v._1.getId + ", Dist: " + v._2)
  }
}