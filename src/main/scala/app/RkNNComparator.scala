package app

import graph.{GraphGen, SGraph}

import util.Utils._

import algorithms.NaiveRkNN.naiveRkNNs
import algorithms.Eager.eager
import algorithms.EmbeddingAlgorithm

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
    // Naive algorithm
    naiveRkNN(sGraph, qID, k)

    // Eager algorithm
    eagerRkNN(sGraph, qID, k)

    // Embedded algorithm
    embeddedRkNN(sGraph, qID, k, numRefPoints = 4)
  }




  def naiveRkNN(jGraph: graph.core.Graph, qID: Integer, k: Integer) : Unit = naiveRkNN(convertJavaToScalaGraph(jGraph), qID, k)
  def naiveRkNN(sGraph: SGraph          , qID: Integer, k: Integer) : Unit = {
    val sQ     = sGraph.getVertex(qID)

    println("-----------Naive:-----------")
    println(s"R${k}NNs for query point " + qID)

    val t0 = System.currentTimeMillis()
    val rkNNsNaive = naiveRkNNs(sGraph, sQ, k)
    val t1 = System.currentTimeMillis()

    println(s"Runtime: ${(t1 - t0)/1000.0} sec.\n")

    for( v <- rkNNsNaive )
      println(s"Node: ${v._1.id}  Dist: ${v._2}")
    println("")
  }

  def eagerRkNN(jGraph: graph.core.Graph, qID: Integer, k: Integer) : Unit =  eagerRkNN(convertJavaToScalaGraph(jGraph), qID, k)
  def eagerRkNN(sGraph: SGraph          , qID: Integer, k: Integer) : Unit = {
    val sQ     = sGraph.getVertex(qID)

    println("-----------Eager:-----------")
    println(s"R${k}NNs for query point " + qID)

    val t0 = System.currentTimeMillis()
    val rkNNsNaive = eager(sGraph, sQ, k)
    val t1 = System.currentTimeMillis()

    println(s"Runtime: ${(t1 - t0)/1000.0} sec.\n")

    for( v <- rkNNsNaive )
      println(s"Node: ${v._1.id}  Dist: ${v._2}")
    println("")
  }

  def embeddedRkNN(jGraph: graph.core.Graph, qID: Integer, k: Integer, numRefPoints: Int) : Unit = embeddedRkNN(convertJavaToScalaGraph(jGraph), qID, k, numRefPoints)
  def embeddedRkNN(sGraph: SGraph          , qID: Integer, k: Integer, numRefPoints: Int) : Unit = {
    val sQ     = sGraph.getVertex(qID)

    println("-----------Embedded:-----------")
    println(s"R${k}NNs for query point $qID")

    val t0            = System.currentTimeMillis()
    val rkNNsEmbedded = EmbeddingAlgorithm.embeddedRKNNs(sGraph, sQ, k, numRefPoints)
    val t1            = System.currentTimeMillis()

    println(s"Runtime: ${(t1 - t0)/1000.0} sec.\n")

    println(rkNNsEmbedded.iter().getDistancePair.getDistance)
//    for( v <- rkNNsEmbedded )
//      println(s"Node: ${v._1.id}  Dist: ${v._2}")
    println("")
  }
}