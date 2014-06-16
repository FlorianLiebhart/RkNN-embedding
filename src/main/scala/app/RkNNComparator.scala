package app

import graph.{SVertex, GraphGen, SGraph}

import util.Utils._
import util.Log
import util.XmlUtil

import algorithms.NaiveRkNN.naiveRkNNs
import algorithms.Eager.eager
import algorithms.{TPL, Embedding}
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

    val exampleGraph = "file" // "eager"  for using the example graph from TKDE - GraphRNN paper page 3,
                             // "tpl"    for using the example graph from the TPL page 748,
                             // "random" for generating a random graph

    val (sGraph, qID, refPoints, k, rStarTreePageSize)  = exampleGraph match {
      case "eager" =>
        val sGraph            = createExampleGraphEager
        val qID               = 4
        val refPoints         = Seq(sGraph.getVertex(1), sGraph.getVertex(2))
        val rStarTreePageSize = 130  // 130 bytes: (minimum for 2 dimensions); Max. entries in node = 3; Max. entries in leaf = 4
                                     // 178 bytes: (minimum for 3 dimensions); Max. entries in node = 3; Max. entries in leaf = 5
        val k                 = 2

        (sGraph, qID, refPoints, k, rStarTreePageSize)

      case "tpl"  =>
//        val sGraph            = convertJavaToScalaGraph(XmlUtil.importGraphFromXml("exampleGraphXMLs/exampleGraphTPL.xml"))
        val sGraph            = convertJavaToScalaGraph(XmlUtil.importGraphFromXml("exampleGraphXMLs/exampleGraphTPLAllObjects.xml"))
        val qID               = 15
        val refPoints         = Seq(sGraph.getVertex(4), sGraph.getVertex(11))
//                                .++(Seq(sGraph.getVertex(13),sGraph.getVertex(5),sGraph.getVertex(16)))

        // 130 bytes: (minimum for 2 dimensions); Max. entries in node = 3; Max. entries in leaf = 4
        // 178 bytes: (minimum for 3 dimensions); Max. entries in node = 3; Max. entries in leaf = 5
        val rStarTreePageSize = refPoints.size match {
          case 1 => 100
          case 2 => 150
          case 3 => 200
          case 4 => 250
          case 5 => 300
        }
        val k                 = 3

        (sGraph, qID, refPoints, k, rStarTreePageSize)

      case "file" =>
        val sGraph            = convertJavaToScalaGraph(XmlUtil.importGraphFromXml("exampleGraphXMLs/1000Nodes4000EdgesAllObjects.xml"))
        val qID               = 200
//        val numRefPoints      = 3
        val rStarTreePageSize = 1024
        val k                 = 3
//        val refPoints         = Embedding.createRefPoints(sGraph.getAllVertices, numRefPoints)
        val refPoints         = Seq(sGraph.getVertex(446), sGraph.getVertex(649), sGraph.getVertex(496))

        (sGraph, qID, refPoints, k, rStarTreePageSize)

      case "random" =>  // randomly generated graph
        val vertices          = 1000
        val objects           = 100
        val edges             = 3500  // from N-1 to N(N-1)/2  // max 2.147.483.647; Vertex max: 65.536
        val qID               = vertices / 2
        val numRefPoints      = 3
        val rStarTreePageSize = 1024  // bytes: e.g. 1024 bytes; Erich recommendation: 25*8*dimensions (=> corresponds to around 25 entries/page)
        val k                 = 2

        val sGraph            = GraphGen.generateScalaGraph(vertices, edges, objects, weightOne = false)

        val refPoints = Embedding.createRefPoints(sGraph.getAllVertices, numRefPoints)

        (sGraph, qID, refPoints, k, rStarTreePageSize)
    }


    /*
     * perfom queries
     */

//    Log.appendln("dijkstra:" + Dijkstra.dijkstra(sGraph, sGraph.getVertex(qID)).size + ", graph vertices" + sGraph.getAllVertices.size)
    Log.appendln("")

    // Naive algorithm
    naiveRkNN(sGraph, qID, k)

    Log.printFlush

    // Eager algorithm
    eagerRkNN(sGraph, qID, k)

    // Embedded algorithm
    embeddedRkNN(sGraph, qID, k, refPoints, rStarTreePageSize)
//    tplRkNN(sGraph, qID, k, refPoints, rStarTreePageSize, withClipping = true)
    Log.printFlush
  }



  def naiveRkNN(jGraph: graph.core.Graph, qID: Integer, k: Integer) : Unit = naiveRkNN(convertJavaToScalaGraph(jGraph), qID, k)
  def naiveRkNN(sGraph: SGraph          , qID: Integer, k: Integer) : Unit = {
    val sQ     = sGraph.getVertex(qID)

    Log.appendln(s"-----------Naive R${k}NN for query point $qID:-----------\n")

    val timeNaiveRkNN  = TimeDiff()

    val rkNNsNaive     = naiveRkNNs(sGraph, sQ, k)

    timeNaiveRkNN.end

    Log.appendln(s"Result r${k}NNs: ${if (rkNNsNaive.size == 0) "--" else ""}")
    for( v <- rkNNsNaive )
      Log.appendln(s"Node: ${v._1.id}  Dist: ${v._2}")

    Log.appendln(s"\nTotal Simple RkNN Runtime: $timeNaiveRkNN \n")
  }

  def eagerRkNN(jGraph: graph.core.Graph, qID: Integer, k: Integer) : Unit = eagerRkNN(convertJavaToScalaGraph(jGraph), qID, k)
  def eagerRkNN(sGraph: SGraph          , qID: Integer, k: Integer) : Unit = {
    val sQ     = sGraph.getVertex(qID)

    Log.appendln(s"-----------Eager R${k}NN for query point $qID:-----------\n")

    val timeEagerRkNN  = TimeDiff()

    val rkNNsEager     = eager(sGraph, sQ, k)

    timeEagerRkNN.end

    Log.appendln(s"Result r${k}NNs: ${if (rkNNsEager.size == 0) "--" else ""}")
    for( v <- rkNNsEager )
      Log.appendln(s"Node: ${v._1.id}  Dist: ${v._2}")

    Log.appendln(s"\nTotal Eager RkNN Runtime: $timeEagerRkNN \n")
  }

  def embeddedRkNN(jGraph: graph.core.Graph, qID: Integer, k: Integer, numRefPoints: Int) : Unit = {
    val sGraph    = convertJavaToScalaGraph(jGraph)
    val refPoints = Embedding.createRefPoints(sGraph.getAllVertices, numRefPoints)
    embeddedRkNN(sGraph, qID, k, refPoints, rStarTreePageSize = 1024)
  }
  def embeddedRkNN(sGraph: SGraph, qID: Integer, k: Integer, refPoints: Seq[SVertex], rStarTreePageSize: Int) : Unit = {
    val sQ     = sGraph.getVertex(qID)

    Log.appendln(s"-----------Embedded R${k}NN for query point $qID:-----------\n")
    Log.printFlush

    val timeEmbeddedRkNN = TimeDiff()

    val rkNNsEmbedded: IndexedSeq[(SVertex, Double)] = Embedding.embeddedRkNNs(sGraph, sQ, k, refPoints, rStarTreePageSize)

    timeEmbeddedRkNN.end

    Log.appendln(s"Result r${k}NNs: ${if (rkNNsEmbedded.size == 0) "--" else ""}")
    for( v <- rkNNsEmbedded )
      Log.appendln(s"Node: ${v._1.id}  Dist: ${v._2}")

    Log.appendln(s"\nTotal Embedding RkNN Runtime: $timeEmbeddedRkNN \n")
  }


  def tplRkNN(sGraph: SGraph, qID: Integer, k: Integer, refPoints: Seq[SVertex], rStarTreePageSize: Int, withClipping: Boolean) : Unit = {
    val sQ     = sGraph.getVertex(qID)

    Log.appendln("-----------TPL:-----------")
    Log.appendln(s"R${k}NNs for query point $qID")

    val timeTPLRkNN                          = TimeDiff()

    val rkNNsTPL: IndexedSeq[(DBID, Double)] = TPL.tplRkNNs(sGraph, sQ, k, refPoints, rStarTreePageSize, withClipping)

    timeTPLRkNN.end

    Log.appendln(s"Runtime: $timeTPLRkNN")

    Log.appendln(s"Result r${k}NNs: ${if (rkNNsTPL.size == 0) "--" else ""}")
    for( v <- rkNNsTPL )
      Log.appendln(s"Node: ${v._1.toString}  Dist: ${v._2}")
    Log.appendln("")
  }
}