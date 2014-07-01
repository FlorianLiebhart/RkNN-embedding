package app

import scala.util.Random

import de.lmu.ifi.dbs.elki.database.ids.DBID

import algorithms.{Eager, Naive, TPL, Embedding}
import graph.GraphGen._
import graph.{SVertex, GraphGen, SGraph}
import util.CPUTimeDiff
import util.Log
import util.XmlUtil


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

    val exampleGraph = "random" // "eager"  for using the example graph from TKDE - GraphRNN paper page 3,
                                // "tpl"    for using the example graph from the TPL page 748,
                                // "random" for generating a random graph

    val (sGraph, q, refPoints, k, rStarTreePageSize)  = exampleGraph match {
      case "eager" =>
        val sGraph            = createExampleGraphEager
        val q                 = sGraph.getVertex(4)
        val refPoints         = Seq(sGraph.getVertex(1), sGraph.getVertex(2))
        val rStarTreePageSize = 130  // 130 bytes: (minimum for 2 dimensions); Max. entries in node = 3; Max. entries in leaf = 4
                                     // 178 bytes: (minimum for 3 dimensions); Max. entries in node = 3; Max. entries in leaf = 5

        val k                 = 2

        if(!q.containsObject)
          q.setObjectId(sGraph.getAllVertices.size)

        (sGraph, q, refPoints, k, rStarTreePageSize)

      case "tpl"  =>
//        val sGraph            = convertJavaToScalaGraph(XmlUtil.importGraphFromXml("exampleGraphXMLs/exampleGraphTPL.xml"))
        val sGraph            = convertJavaToScalaGraph(XmlUtil.importGraphFromXml("exampleGraphXMLs/exampleGraphTPLAllObjects.xml"))
        val q                 = sGraph.getVertex(15)
        val refPoints         = Seq(sGraph.getVertex(4))//, sGraph.getVertex(11))
//                                .++(Seq(sGraph.getVertex(13) ,sGraph.getVertex(5)))//, sGraph.getVertex(16)))

        val rStarTreePageSize = (refPoints.size * 16 * 3 ) + 34 // minimum!

//          case 1 => 82 // 82 bytes: (min 1 dim); Max entr. node = 3; Max entr. leaf = 4
//          case 2 => 130 // 130 bytes: (min 2 dim); Max entr. node = 3; Max entr. leaf = 4
//          case 3 => 178 // 178 bytes: (min 3 dim); Max entr. node = 3; Max entr. leaf = 5
//          case 4 => 226 // 226 bytes: (min 4 dim); Max entr. node = 3; Max entr. leaf = 5
//          case 5 => 274 // 274 bytes: (min 5 dim); Max entr. node = 3; Max entr. leaf = 5

//          case 5 => 304 // 304 bytes: (min 5 dim); Max entr. node = 3; Max entr. leaf = 6
//          case 5 => 354 // 354 bytes: (min 5 dim); Max entr. node = 3; Max entr. leaf = 7

//          case 5 => 364 // 364 bytes: (min 5 dim); Max entr. node = 4; Max entr. leaf = 7
//          case 5 => 404 // 404 bytes: (min 5 dim); Max entr. node = 4; Max entr. leaf = 8

//          case 5 => 454 // 454 bytes: (min 5 dim); Max entr. node = 5; Max entr. leaf = 9
//          case 5 => 504 // 504 bytes: (min 5 dim); Max entr. node = 5; Max entr. leaf = 10

//          case 5 => 544 // 544 bytes: (min 5 dim); Max entr. node = 6; ...
//          case 5 => 634 // 634 bytes: (min 5 dim); Max entr. node = 7; ...
//          case 5 => 724 // 724 bytes: (min 5 dim); Max entr. node = 8; ...
//          case 5 => 814 // 814 bytes: (min 5 dim); Max entr. node = 9; ...
//          case 5 => 904 // 904 bytes: (min 5 dim); Max entr. node = 10; ...

        val k                 = 3

        if(!q.containsObject)
          q.setObjectId(sGraph.getAllVertices.size)

        (sGraph, q, refPoints, k, rStarTreePageSize)

      case "file" =>
        val sGraph            = convertJavaToScalaGraph(XmlUtil.importGraphFromXml("exampleGraphXMLs/1000Nodes4000EdgesAllObjects.xml"))
        val q                 = sGraph.getVertex(200)
        val rStarTreePageSize = 1024
        val k                 = 3
//        val numRefPoints      = 3
//        val refPoints         = Embedding.createRefPoints(sGraph.getAllVertices, numRefPoints)
        val refPoints         = Seq(sGraph.getVertex(446), sGraph.getVertex(649), sGraph.getVertex(496))

        if(!q.containsObject)
          q.setObjectId(sGraph.getAllVertices.size)

        (sGraph, q, refPoints, k, rStarTreePageSize)

      case "random" =>  // randomly generated graph
        // vertices may be a little less than what defined here, since the floored sqrt will be squared
        val vertices          = 100 // Max. 1 Million! (so that there won't be an integer overflow for max-edges)
        val actualVertices    = Math.pow(Math.sqrt(vertices).floor, 2).toInt

        val objects           = 1 //0.005 * actualVertices

        val nrOfRowsAndCols   = Math.sqrt(actualVertices)
        val rowEdges          = nrOfRowsAndCols * (nrOfRowsAndCols - 1)
        val minEdges          = rowEdges + (nrOfRowsAndCols - 1)
        val maxEdges          = (nrOfRowsAndCols - 1) * (Math.pow(nrOfRowsAndCols, 2))    // Maximum Edges: all edges between all rows: cols * (rows - 1)
                                                                                          //             +  all edges between each col:  (cols - 1) * (rows * rows)

        val edges             = 0.1 * (maxEdges - minEdges) + minEdges   // generally for a graph: from N-1 to N(N-1)/2 // Int Overflow at: max 2.147.483.647 => Vertex max: 65.536

        val qID               = new Random(System.currentTimeMillis).nextInt(actualVertices+1)
        val numRefPoints      = 30
        val rStarTreePageSize = 25 * 8 * numRefPoints  // bytes: e.g. 1024 bytes; Erich recommendation: 25*8*dimensions (=> corresponds to around 25 entries/page)
        val k                 = 3

        val sGraph            = GraphGen.generateScalaGraph(actualVertices, edges.toInt, objects.toInt, edgeMaxWeight = 10)
        val q                 = sGraph.getVertex(qID)

//        val jGraph            = convertScalaToJavaGraph(sGraph)
//        XmlUtil.saveGraphToXml(jGraph, "exampleGraphXMLs/generatedGraph.xml")
        val refPoints = Embedding.createRefPoints(sGraph.getAllVertices, numRefPoints)

        if(!q.containsObject)
          q.setObjectId(sGraph.getAllVertices.size)

        (sGraph, q, refPoints, k, rStarTreePageSize)
    }


    /*
     * perfom queries
     */

//    Log.appendln("dijkstra:" + Dijkstra.dijkstra(sGraph, sGraph.getVertex(qID)).size + ", graph vertices" + sGraph.getAllVertices.size)
    Log.appendln("")

    // Naive algorithm
//    naiveRkNN(sGraph, qID, k)

    Log.printFlush

    // Eager algorithm
    eagerRkNN(sGraph, q, k)

    // Embedded algorithm
    embeddedRkNN(sGraph, q, k, refPoints, rStarTreePageSize)
//    tplRkNN(sGraph, qID, k, refPoints, rStarTreePageSize, withClipping = true)
    Log.printFlush
  }


  def naiveRkNN(sGraph: SGraph, q: SVertex, k: Integer) : Unit = {

    Log.appendln(s"-----------Naive R${k}NN for query point ${q.id}:-----------\n").printFlush

    val timeNaiveRkNN  = CPUTimeDiff()

    val rknns          = Naive.rknns(sGraph, q, k)

    timeNaiveRkNN.end

    Log.appendln(s"Result r${k}NNs: ${if (rknns.size == 0) "--" else ""}")
    for( v <- rknns )
      Log.appendln(s"Node: ${v._1.id}  Dist: ${v._2}")

    Log.appendln(s"\nTotal Simple RkNN Runtime: $timeNaiveRkNN \n")
  }

  def eagerRkNN(sGraph: SGraph, q: SVertex, k: Integer) : Unit = {

    Log.appendln(s"-----------Eager R${k}NN for query point ${q.id}:-----------\n")

    val timeEagerRkNN  = CPUTimeDiff()

    val rknns          = Eager.rknns(sGraph, q, k)

    timeEagerRkNN.end

    Log.appendln(s"Result r${k}NNs: ${if (rknns.size == 0) "--" else ""}")
    for( v <- rknns )
      Log.appendln(s"Node: ${v._1.id}  Dist: ${v._2}")

    Log.appendln(s"\nTotal Eager RkNN Runtime: $timeEagerRkNN \n")
  }

  def embeddedRkNN(sGraph: SGraph, q: SVertex, k: Integer, refPoints: Seq[SVertex], rStarTreePageSize: Int) : Unit = {

    Log.appendln(s"-----------Embedded R${k}NN for query point ${q.id}:-----------\n")
    Log.printFlush

    val timeEmbeddedRkNN = CPUTimeDiff()

    val (relation, rStarTree, dbidVertexIDMapping) = Embedding.createDatabaseWithIndex(sGraph, -1, rStarTreePageSize, "filedatabase/rTree.csv", refPoints)
    val queryObject                                = Embedding.getQueryObject(relation, q, dbidVertexIDMapping)
    val rknns: Seq[(SVertex, Double)] = Embedding.rknns(sGraph, q, k, relation, queryObject , rStarTree, dbidVertexIDMapping)

    timeEmbeddedRkNN.end

    Log.appendln(s"Result r${k}NNs: ${if (rknns.size == 0) "--" else ""}")
    for( v <- rknns )
      Log.appendln(s"Node: ${v._1.id}  Dist: ${v._2}")

    Log.appendln(s"\nTotal Embedding RkNN Runtime: $timeEmbeddedRkNN \n")
  }


  def tplRkNN(sGraph: SGraph, q: SVertex, k: Integer, refPoints: Seq[SVertex], rStarTreePageSize: Int, withClipping: Boolean) : Unit = {
    Log.appendln("-----------TPL:-----------")
    Log.appendln(s"R${k}NNs for query point ${q.id}")

    val timeTPLRkNN                          = CPUTimeDiff()

    val rkNNsTPL: Seq[(DBID, Double)] = TPL.tplRkNNs(sGraph, q, k, refPoints, rStarTreePageSize, withClipping)

    timeTPLRkNN.end

    Log.appendln(s"Runtime: $timeTPLRkNN")

    Log.appendln(s"Result r${k}NNs: ${if (rkNNsTPL.size == 0) "--" else ""}")
    for( v <- rkNNsTPL )
      Log.appendln(s"Node: ${v._1.toString}  Dist: ${v._2}")
    Log.appendln("")
  }
}