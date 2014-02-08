package app

import algorithms.Eager.eager
import algorithms.NaiveRkNN.getReverseKNearestNeighbors
import graph.{GraphGen, SEdge, SGraph, SVertex}
import util.Utils._
import util.{GraphAlgorithm, Utils}
import algorithms.Eager.eager
import algorithms.EmbeddingAlgorithm._
import java.util.Random
import algorithms.Dijkstra

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

//    val sGraph = Utils.createExampleGraph
//    val qID = 5

    // Graph Generation
    val vertices = 1000
    val edges = 1500
    val objects = 100
    val weightOne = false
    val sGraph = GraphGen.generateScalaGraph(vertices, edges, objects, weightOne)
    val qID = vertices/2
      if(!sGraph.getVertex(qID).containsObject)
        sGraph.getVertex(qID).setObjectId(objects)

    val k = 2

//    println("dijkstra:" + Dijkstra.dijkstra(sGraph, sGraph.getVertex(qID)).size + ", graph vertices" + sGraph.getAllVertices.size)

    println("")
    // Naive - Algorithm
    naiveRkNN(sGraph, qID, k)

    // Eager - Algorithm
    eagerRkNN(sGraph, qID, k)
  }




  def naiveRkNN(jGraph: graph.core.Graph, qID: Integer, k: Integer) : Unit = naiveRkNN(convertJavaToScalaGraph(jGraph), qID, k)

  def naiveRkNN(sGraph: SGraph, qID: Integer, k: Integer) : Unit = {
    val sQ     = sGraph.getVertex(qID)

    println("-----------Naive:-----------")
    println("R" + k + "NNs for query point " + qID)

    val t0 = System.currentTimeMillis()
    val rKnnsNaive = getReverseKNearestNeighbors(sGraph, sQ, k)
    val t1 = System.currentTimeMillis()

    println("Runtime: " + (t1 - t0)/1000.0 + " sec.\n")

    for( v <- rKnnsNaive )
      println("Node: " + v._1.id + "  Dist: " + v._2)
    println("")
  }

  def eagerRkNN(jGraph: graph.core.Graph, qID: Integer, k: Integer) : Unit =  eagerRkNN(convertJavaToScalaGraph(jGraph), qID, k)

  def eagerRkNN(sGraph: SGraph, qID: Integer, k: Integer) : Unit = {
    val sQ     = sGraph.getVertex(qID)

    println("-----------Eager:-----------")
    println("R" + k + "NNs for query point " + qID)

    val t0 = System.currentTimeMillis()
    val rKnnsNaive = eager(sGraph, sQ, k)
    val t1 = System.currentTimeMillis()

    println("Runtime: " + (t1 - t0)/1000.0 + "sec.\n")

    for( v <- rKnnsNaive )
      println("Node: " + v._1.id + "  Dist: " + v._2)
    println("")
  }

    def embeddedRkNN(jGraph: graph.core.Graph, qID: Integer, k: Integer) : Unit = embeddedRkNN(convertJavaToScalaGraph(jGraph), qID, k)

    def embeddedRkNN(sGraph: SGraph, qID: Integer, k: Integer) : Unit = {
    val sQ     = sGraph.getVertex(qID)

    println("-----------Embedded:-----------")
    println("R" + k + "NNs for query point " + qID)

    val t0 = System.currentTimeMillis()
    val rKnnsNaive = getReverseKNearestNeighbors(sGraph, sQ, k)
    val t1 = System.currentTimeMillis()

    println("Runtime: " + (t1 - t0)/1000.0 + " sec.\n")

    for( v <- rKnnsNaive )
      println("Node: " + v._1.id + "  Dist: " + v._2)
    println("")

  }
}