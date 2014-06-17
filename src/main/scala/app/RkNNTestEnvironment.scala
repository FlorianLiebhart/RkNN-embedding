package app

import graph.{SGraph, GraphGen, SVertex}

import util.Utils._
import util.Log
import util.XmlUtil

import algorithms.NaiveRkNN.naiveRkNNs
import algorithms.Eager.eager
import algorithms.{Embedding}
import scala.util.Random
import java.util.Date
import java.text.SimpleDateFormat

object RkNNTestEnvironment {


  def main(args: Array[String]): Unit = {

    val date = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date())
    Log.appendln(s"###### Start: $date ######\n")
    Log.writeFlushWriteLog(appendToFile = false)

    try {
      runExperiments()
    }
    catch {
      case e: Exception => {
        Log.appendln(e.getStackTraceString)
        Log.writeFlushWriteLog(appendToFile = true)
        Log.writeFlushExperimentLog(appendToFile = false, name = "ERROR")
        Log.printFlush
        e.printStackTrace
      }
    }
    Log.printFlush
    Log.writeFlushWriteLog(appendToFile = true)
  }

  def runExperiments() {
    val shortExperiments      = false
    val experimentRepeatTimes = if (shortExperiments) 1 else 2

    println("Code organization phase: Performing dry run (running all algorithms once)")
    dryRun()
    println("Dry run completed. Now starting experiments.\n\n")


    println("--------------- Starting experiments. -----------------\n")


    val settings = new TestSettings()

    /*
     * 0. Default run
     */
    settings.default()

    Log.experimentLogAppendln("----------------------------------------------------------------------")
    Log.experimentLogAppendln(s"0. Experiment: Default run.")
    Log.experimentLogAppendln("----------------------------------------------------------------------\n")

    Log.experimentLogAppend(s"$settings\n")

    Log.writeFlushExperimentLog(name = "0 Default", appendToFile = false)
    runExperiment(experimentRepeatTimes, settings, valueName = "Default values", value = "")
    Log.writeFlushExperimentLog(name = "0 Default", appendToFile = true)
    Log.writeFlushWriteLog(true)


    /*
     * 1. Alter entries per node (r*tree page size)
     */
    settings.default()

    Log.experimentLogAppendln("----------------------------------------------------------------------")
    Log.experimentLogAppendln(s"1. Experiment: Alter number of entries per node (r*tree page size).")
    Log.experimentLogAppendln("----------------------------------------------------------------------\n")

    Log.experimentLogAppend(s"$settings\n")
    Log.appendln(s"$settings\n")
    Log.writeFlushWriteLog(true)

    var valuesEntry = Seq(5, 10, 15, 20, 25, 30, 35, 40, 45, 50)
    if (shortExperiments) valuesEntry = valuesEntry.dropRight(6)

    Log.writeFlushExperimentLog(name = "1 Entries", appendToFile = false)
    for(entriesPerNode <- valuesEntry){
      settings.entriesPerNode = entriesPerNode
      runExperiment(experimentRepeatTimes, settings, valueName = "Entries per node", value = entriesPerNode, onlyEmbedding = true)
      Log.writeFlushExperimentLog(name = "1 Entries", appendToFile = true)
      Log.writeFlushWriteLog(true)
    }

    /*
     * 2. Alter number of reference points
     */
    settings.default()

    Log.experimentLogAppendln("----------------------------------------------------------------------")
    Log.experimentLogAppendln(s"2. Experiment: Alter number of reference points.")
    Log.experimentLogAppendln("----------------------------------------------------------------------\n")

    Log.experimentLogAppend(s"$settings\n")
    Log.appendln(s"$settings\n")
    Log.writeFlushWriteLog(true)

    var valuesRefPoints = Seq(5, 10, 15, 20, 25, 30, 35, 40, 45, 50)
    if (shortExperiments) valuesRefPoints = valuesRefPoints.dropRight(6)

    Log.writeFlushExperimentLog(name = "2 Refs", appendToFile = false)
    for(refPoints <- valuesRefPoints){
      settings.numRefPoints = refPoints
      runExperiment(experimentRepeatTimes, settings, valueName = "Reference points", value = refPoints, onlyEmbedding = true)
      Log.writeFlushExperimentLog(name = "2 Refs", appendToFile = true)
      Log.writeFlushWriteLog(true)
    }


    /*
     * 3. Alter Vertices
     */
    settings.default()

    Log.experimentLogAppendln("----------------------------------------------------------------------")
    Log.experimentLogAppendln(s"2. Experiment: Alter number of vertices.")
    Log.experimentLogAppendln("----------------------------------------------------------------------\n")

    Log.experimentLogAppend(s"$settings\n")
    Log.appendln(s"$settings\n")
    Log.writeFlushWriteLog(true)

    var valuesVertices = Seq(10, 100, 1000, 10000, 50000, 100000, 200000, 500000)
    if (shortExperiments) valuesVertices = valuesVertices.dropRight(3)

    Log.writeFlushExperimentLog(name = "3 Vertices", appendToFile = false)
    for(vertices <- valuesVertices){
      settings.approximateVertices = vertices
      runExperiment(experimentRepeatTimes, settings, valueName = "Vertices", value = vertices)
      Log.writeFlushExperimentLog(name = "3 Vertices", appendToFile = true)
      Log.writeFlushWriteLog(true)
    }


    /*
     * 4. Alter Object Density
     */
    settings.default()

    Log.experimentLogAppendln("----------------------------------------------------------------------")
    Log.experimentLogAppendln(s"4. Experiment: Alter object density.")
    Log.experimentLogAppendln("----------------------------------------------------------------------\n")

    Log.experimentLogAppend(s"$settings\n")
    Log.appendln(s"$settings\n")
    Log.writeFlushWriteLog(true)

    var valuesODensity = Seq(0.005, 0.01, 0.02, 0.04, 0.08, 0.16, 0.32, 0.64, 1.0)
    if (shortExperiments) valuesODensity = valuesODensity.drop(3).dropRight(3)

    Log.writeFlushExperimentLog(name = "4 Objects", appendToFile = false)
    for(oDensity <- valuesODensity){
      settings.objectDensity = oDensity
      runExperiment(experimentRepeatTimes, settings, valueName = "Object density", value = oDensity)
      Log.writeFlushExperimentLog(name = "4 Objects", appendToFile = true)
      Log.writeFlushWriteLog(true)
    }


    /*
     * 5. Alter Connectivity (Edges)
     */
    settings.default()

    Log.experimentLogAppendln("----------------------------------------------------------------------")
    Log.experimentLogAppendln(s"5. Experiment: Alter connectivity (number of edges).")
    Log.experimentLogAppendln("----------------------------------------------------------------------\n")

    Log.experimentLogAppend(s"$settings\n")
    Log.appendln(s"$settings\n")
    Log.writeFlushWriteLog(true)

    var valuesConnectivity = Seq(0.0, 0.005, 0.01, 0.02, 0.04, 0.08, 0.16, 0.32, 0.64, 1.0)
    if (shortExperiments) valuesConnectivity = valuesConnectivity.drop(2).dropRight(3)

    Log.writeFlushExperimentLog(name = "5 Edges", appendToFile = false)
    for(connectivity <- valuesConnectivity){
      settings.connectivity = connectivity
      runExperiment(experimentRepeatTimes, settings, valueName = "Connectivity", value = connectivity)
      Log.writeFlushExperimentLog(name = "5 Edges", appendToFile = true)
      Log.writeFlushWriteLog(true)
    }


    /*
     * 6. Alter k
     */
    settings.default()

    Log.experimentLogAppendln("----------------------------------------------------------------------")
    Log.experimentLogAppendln(s"6. Experiment: Alter k.")
    Log.experimentLogAppendln("----------------------------------------------------------------------\n")

    Log.experimentLogAppend(s"$settings\n")
    Log.appendln(s"$settings\n")
    Log.writeFlushWriteLog(true)
    
    var valuesK = Seq(1, 2, 4, 8, 16)
    if (shortExperiments) valuesK = valuesK.drop(1).dropRight(1)

    Log.writeFlushExperimentLog(name = "6 k", appendToFile = false)
    for(k <- valuesK){
      settings.k = k
      runExperiment(experimentRepeatTimes, settings, valueName = "k", value = k)
      Log.writeFlushExperimentLog(name = "6 k", appendToFile = true)
      Log.writeFlushWriteLog(true)
    }

    println("------------- All experiments finished. ---------------\n")
  }

  /**
   * Create a graph and run all three algorithms. Do that "times" times, and calculate an average runtime.
   * @param times
   * @param settings
   * @param valueName
   * @param value
   * @return
   */
  def runExperiment(times: Int, settings: TestSettings, valueName: String, value: Any, onlyEmbedding: Boolean = false) = {

    var naiveNodesToVerify       = 0
    var naiveNodesVisited        = 0
    var naiveRunTimeRknnQuery    = 0

    var eagerNodesToVerify       = 0
    var eagerNodesVisited        = 0
    var eagerRunTimeRknnQuery    = 0


    var embeddedNodesToVerify       = 0
    var embeddedNodesVisited        = 0
    var embeddingFilteredCandidates = 0
    var embeddedRunTimeRknnQuery    = 0
    var embeddedRunTimePreparation  = 0

    Log.experimentLogAppendln(s"$valueName: $value")
    Log.experimentLogAppendln("---------------------\n")


    for(i <- 1 to times) {
      val sGraph = GraphGen.generateScalaGraph(settings.vertices, settings.edges, settings.objects)
      settings.qID = new Random(System.currentTimeMillis).nextInt(settings.vertices)

      if (!onlyEmbedding) {
        Log.resetStats()

        naiveRkNN(sGraph, settings)
        naiveNodesToVerify    += (Log.nodesToVerify    / times).toInt
        naiveNodesVisited     += (Log.nodesVisited     / times).toInt
        naiveRunTimeRknnQuery += (Log.runTimeRknnQuery / times).toInt

        Log.resetStats()

        eagerRkNN(sGraph, settings)
        eagerNodesToVerify    += (Log.nodesToVerify    / times).toInt
        eagerNodesVisited     += (Log.nodesVisited     / times).toInt
        eagerRunTimeRknnQuery += (Log.runTimeRknnQuery / times).toInt
      }

      Log.resetStats()

      embeddedRkNN(sGraph, settings)
      embeddedNodesToVerify       += (Log.nodesToVerify               / times).toInt
      embeddedNodesVisited        += (Log.nodesVisited                / times).toInt
      embeddedRunTimePreparation  += (Log.runTimeEmbeddingPreparation / times).toInt
      embeddedRunTimeRknnQuery    += (Log.runTimeRknnQuery            / times).toInt

      embeddingFilteredCandidates += (Log.embeddingFilteredCandidates / times).toInt
    }

    if (!onlyEmbedding) {
      Log.experimentLogAppendln("Naive:\n")

      Log.experimentLogAppendln(s"Candidates to refine on graph:     $naiveNodesToVerify")
      Log.experimentLogAppendln(s"Nodes visited:                     $naiveNodesVisited")
      Log.experimentLogAppendln(s"Total rknn query runtime:          $naiveRunTimeRknnQuery ms.\n")

      Log.experimentLogAppendln("Eager:\n")

      Log.experimentLogAppendln(s"Candidates to refine on graph:     $eagerNodesToVerify")
      Log.experimentLogAppendln(s"Nodes visited:                     $eagerNodesVisited")
      Log.experimentLogAppendln(s"Total rknn query runtime:          $eagerRunTimeRknnQuery ms.\n")
    }

    Log.experimentLogAppendln("Embedding:\n")

    Log.experimentLogAppendln(s"Candidates left after filter:      $embeddingFilteredCandidates")
    Log.experimentLogAppendln(s"Candidates to refine on graph:     $embeddedNodesToVerify")
    Log.experimentLogAppendln(s"Nodes visited:                     $embeddedNodesVisited")
    Log.experimentLogAppendln(s"Runtime for embedding preparation: $embeddedRunTimePreparation ms.")
    Log.experimentLogAppendln(s"Total rknn query runtime:          $embeddedRunTimeRknnQuery ms.\n\n")
  }


  def naiveRkNN(sGraph: SGraph, settings: TestSettings) : Unit = {
    val k      = settings.k
    val qID    = settings.qID
    val q      = sGraph.getVertex(qID)

    Log.appendln(s"\n-----------Naive R${k}NN for query point $q:-----------\n")

    val timeNaiveRkNN  = TimeDiff()

    val rkNNsNaive     = naiveRkNNs(sGraph, q, k)

    timeNaiveRkNN.end
    Log.runTimeRknnQuery = timeNaiveRkNN.diff

    Log.appendln(s"Result r${k}NNs: ${if (rkNNsNaive.size == 0) "--" else ""}")
    for( v <- rkNNsNaive )
      Log.appendln(s"Node: ${v._1.id}  Dist: ${v._2}")

    Log.appendln(s"\nTotal Simple RkNN Runtime: $timeNaiveRkNN \n")
    Log.printFlush
  }

  def eagerRkNN(sGraph: SGraph, settings: TestSettings) : Unit = {
    val k      = settings.k
    val qID    = settings.qID
    val q      = sGraph.getVertex(qID)

    Log.appendln(s"-----------Eager R${k}NN for query point $q:-----------\n")

    val timeEagerRkNN  = TimeDiff()

    val rkNNsEager     = eager(sGraph, q, k)

    timeEagerRkNN.end
    Log.runTimeRknnQuery = timeEagerRkNN.diff

    Log.appendln(s"Result r${k}NNs: ${if (rkNNsEager.size == 0) "--" else ""}")
    for( v <- rkNNsEager )
      Log.appendln(s"Node: ${v._1.id}  Dist: ${v._2}")

    Log.appendln(s"\nTotal Eager RkNN Runtime: $timeEagerRkNN \n")
  }

  def embeddedRkNN(sGraph: SGraph, settings: TestSettings) : Unit = {
    val k                 = settings.k
    val rStarTreePageSize = settings.rStarTreePageSize
    val qID               = settings.qID
    val numRefPoints      = settings.numRefPoints
    val q                 = sGraph.getVertex(qID)

    Log.appendln(s"-----------Embedded R${k}NN for query point $qID:-----------\n")
    Log.printFlush

    val timeEmbeddedRkNN = TimeDiff()

    val rkNNsEmbedded: Seq[(SVertex, Double)] = Embedding.embeddedRkNNs(sGraph, q, k, numRefPoints, rStarTreePageSize)

    timeEmbeddedRkNN.end

    Log.appendln(s"Result r${k}NNs: ${if (rkNNsEmbedded.size == 0) "--" else ""}")
    for( v <- rkNNsEmbedded )
      Log.appendln(s"Node: ${v._1.id}  Dist: ${v._2}")

    Log.appendln(s"\nTotal Embedding RkNN Runtime: $timeEmbeddedRkNN \n")
    Log.printFlush
  }

  /**
   *  Code-Organization: Perform all algorithms prior running the tests, so that the JVM can organize code.
   */
  def dryRun() = {
    val settings = new TestSettings(
      approximateVertices = 1000,
      objectDensity       = 0.05,
      connectivity        = 0.1,
      k                   = 3,
      numRefPoints        = 3,
      entriesPerNode      = 25
    )

    val sGraph = GraphGen.generateScalaGraph(settings.vertices, settings.edges, settings.objects)
    settings.qID = new Random(System.currentTimeMillis).nextInt(settings.vertices)

    naiveRkNN(sGraph, settings)
    eagerRkNN(sGraph, settings)
    embeddedRkNN(sGraph, settings)
  }
}