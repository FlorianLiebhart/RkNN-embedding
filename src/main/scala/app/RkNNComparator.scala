package app

import algorithms.Eager.eager
import algorithms.NaiveRkNN.getReverseKNearestNeighbors
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

    val rKnnsNaive = getReverseKNearestNeighbors(graph, graph.getVertex(5), 2)
    println("Naive:")
    for( v <- rKnnsNaive )
      println("Node: " + v._1.getId + ", Dist: " + v._2)

    println("\nEager:")
    val rKnnsEager = eager(graph, graph.getVertex(5),2)
    for( v <- rKnnsEager )
        println("Node: " + v._1.getId + ", Dist: " + v._2)
    }
}