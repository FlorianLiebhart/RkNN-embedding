package app

import graph.{SVertex, GraphGen, SGraph}

import util.Utils._

import algorithms.NaiveRkNN.naiveRkNNs
import algorithms.Eager.eager
import algorithms.EmbeddingAlgorithm
import de.lmu.ifi.dbs.elki.database.ids.DBID

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


    /*
     * rknn query settings
     */

    val exampleGraph = true  // true for using the example graph from TKDE - GraphRNN paper page 3,
                             // false for generating a random graph
    val (sGraph, qID, refPoints, k, rStarTreePageSize)  =
      if (exampleGraph) {
        val sGraph            = createExampleGraph
        val qID               = 4
        val refPoints         = Seq(sGraph.getVertex(1), sGraph.getVertex(2))
        val rStarTreePageSize = 130  // 130 bytes: Maximum entries in a directory node = 3; Maximum entries in a leaf node = 4
        val k                 = 2

        (sGraph, qID, refPoints, k, rStarTreePageSize)
      }
      else {   // randomly generated graph
        val vertices          = 1000
        val objects           = 100
        val edges             = 3500 // from N-1 to N(N-1)/2  // max 2.147.483.647; Vertex max: 65.536
        val qID               = vertices / 2
        val numRefPoints      = 3
        val rStarTreePageSize = 1024  // bytes: e.g. 1024 bytes; Erich recommendation: 25*8*dimensions (=> corresponds to around 25 entries/page)
        val k                 = 2

        val sGraph            = GraphGen.generateScalaGraph(vertices, edges, objects, weightOne = false)

        // insert a new object in query node, if non existent
        if (!sGraph.getVertex(qID).containsObject)
          sGraph.getVertex(qID).setObjectId(objects) // generated object IDs start with 0

        val refPoints = EmbeddingAlgorithm.createRefPoints(sGraph.getAllVertices, numRefPoints)

        (sGraph, qID, refPoints, k, rStarTreePageSize)
      }


    /*
     * perfom queries
     */

//    println("dijkstra:" + Dijkstra.dijkstra(sGraph, sGraph.getVertex(qID)).size + ", graph vertices" + sGraph.getAllVertices.size)
    println("")

    // Naive algorithm
    naiveRkNN(sGraph, qID, k)

    // Eager algorithm
    eagerRkNN(sGraph, qID, k)

    // Embedded algorithm
    embeddedRkNN(sGraph, qID, k, refPoints, rStarTreePageSize)
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

    println(s"Result r${k}NNs: ${if (rkNNsNaive.size == 0) "--" else ""}")
    for( v <- rkNNsNaive )
      println(s"Node: ${v._1.id}  Dist: ${v._2}")
    println("")
  }

  def eagerRkNN(jGraph: graph.core.Graph, qID: Integer, k: Integer) : Unit = eagerRkNN(convertJavaToScalaGraph(jGraph), qID, k)
  def eagerRkNN(sGraph: SGraph          , qID: Integer, k: Integer) : Unit = {
    val sQ     = sGraph.getVertex(qID)

    println("-----------Eager:-----------")
    println(s"R${k}NNs for query point " + qID)

    val t0 = System.currentTimeMillis()
    val rkNNsEager = eager(sGraph, sQ, k)
    val t1 = System.currentTimeMillis()

    println(s"Runtime: ${(t1 - t0)/1000.0} sec.\n")

    println(s"Result r${k}NNs: ${if (rkNNsEager.size == 0) "--" else ""}")
    for( v <- rkNNsEager )
      println(s"Node: ${v._1.id}  Dist: ${v._2}")
    println("")
  }

  def embeddedRkNN(jGraph: graph.core.Graph, qID: Integer, k: Integer, numRefPoints: Int) : Unit = {
    val sGraph    = convertJavaToScalaGraph(jGraph)
    val refPoints = EmbeddingAlgorithm.createRefPoints(sGraph.getAllVertices, numRefPoints)
    embeddedRkNN(sGraph, qID, k, refPoints, rStarTreePageSize = 1024)
  }
  def embeddedRkNN(sGraph: SGraph          , qID: Integer, k: Integer, refPoints: Seq[SVertex], rStarTreePageSize: Int) : Unit = {
    val sQ     = sGraph.getVertex(qID)

    println("-----------Embedded:-----------")
    println(s"R${k}NNs for query point $qID")

    val t0            = System.currentTimeMillis()
    val rkNNsEmbedded: IndexedSeq[(DBID, Double)] = EmbeddingAlgorithm.embeddedRKNNs(sGraph, sQ, k, refPoints, rStarTreePageSize)
    val t1            = System.currentTimeMillis()

    println(s"Runtime: ${(t1 - t0)/1000.0} sec.\n")

    println(s"Result r${k}NNs: ${if (rkNNsEmbedded.size == 0) "--" else ""}")
    for( v <- rkNNsEmbedded )
      println(s"Node: ${v._1.toString}  Dist: ${v._2}")
    println("")
  }
}