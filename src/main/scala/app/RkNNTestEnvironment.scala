package app

import graph.{SGraph, SVertex}

import util.Utils._
import util.Log

import algorithms.NaiveRkNN.naiveRkNNs
import algorithms.Eager.eager
import algorithms.{Embedding}
import java.util.Date
import java.text.SimpleDateFormat

object RkNNTestEnvironment {

  def main(args: Array[String]): Unit = {

    val date = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date())
    Log.appendln(s"###### Start: $date ######\n")
    Log.writeFlushWriteLog(appendToFile = false)

    try {
      runExperiments(
        shortExperiments = true,
        runs             = 2
      )
    }
    catch {
      case e: Exception => {
        Log.appendln(e.getStackTraceString)
        Log.writeFlushWriteLog(appendToFile = true)
        Log.printFlush
        e.printStackTrace
      }
    }
    Log.printFlush
    Log.writeFlushWriteLog(appendToFile = true)
  }

  def runExperiments(runs: Int, shortExperiments: Boolean) {
//    dryRun()

    println("--------------- Starting experiments. -----------------\n")

    expDefault      (runs)                      //0. Default run
//    expEntries      (runs, shortExperiments)    //1. Alter entries per node (r*tree page size)
//    expRefPoints    (runs, shortExperiments)    //2. Alter number of reference points
//    expVertices     (runs, shortExperiments)    //3. Alter Vertices
//    expObjectDensity(runs, shortExperiments)    //4. Alter Object Density
//    expConnectivity (runs, shortExperiments)    //5. Alter Connectivity (Edges)
//    expK            (runs, shortExperiments)    //6. Alter k

    println("\n------------- All experiments finished. ---------------\n")
  }

  /**
   * 0. Default run
   */
  def expDefault(runs: Int) = {
    val setup =
      new ExperimentSetup(
        experimentValueName = Experiment.default,
        experimentValue     = -1.0,
        runs                = runs
    )

    val experimentResult: ExperimentResult = new ExperimentResult(
      experimentTitle = "0. Experiment: Default run.",
      experimentValueName = Experiment.default,
      values = Seq(),
      Seq[Seq[AlgorithmResult]](),
      writeName = "0. Default run"
    )

    experimentResult.write()

    experimentResult.algorithmResultsForEachValue =
      Seq(
        Seq[AlgorithmResult](
          runExperimentNaive(setup),
          runExperimentEager(setup),
          runExperimentEmbedded(setup)
        )
      )

    experimentResult.write()
  }

  /**
   * 1. Alter entries per node (R*Tree page size)
   */
  def expEntries(runs: Int, shortExperiments: Boolean) = {
    var values = Seq(5, 10, 15, 20, 25, 30, 35, 40, 45, 50)
    if (shortExperiments) values = values.dropRight(6)

    val experimentSetups = values map { value =>
      ExperimentSetup(
        experimentValueName = Experiment.entriesPerNode,
        experimentValue = value,
        numRefPoints = value,
        runs = runs
      )
    }

    val experimentResult: ExperimentResult = new ExperimentResult(
      experimentTitle = "1. Experiment: Alter number of entries per node (R*Tree page size).",
      experimentValueName = Experiment.entriesPerNode,
      values,
      Seq[Seq[AlgorithmResult]](),
      writeName = "1. Entries per node"
    )

    experimentResult.write()

    for(setup <- experimentSetups) {
      experimentResult.algorithmResultsForEachValue :+=
        Seq[AlgorithmResult](
          runExperimentEmbedded(setup)
        )
      experimentResult.write()
    }
  }

  /**
   * 2. Alter number of reference points
   */
  def expRefPoints(runs: Int, shortExperiments: Boolean) = {
    var values = Seq(10, 100, 1000, 10000, 50000, 100000, 200000, 500000)
    if (shortExperiments) values = values.dropRight(6)

    val experimentSetups = values map { value =>
      ExperimentSetup(
        experimentValueName = Experiment.refPoints,
        experimentValue = value,
        numRefPoints = value,
        runs = runs
      )
    }

    val experimentResult: ExperimentResult = new ExperimentResult(
      experimentTitle = "2. Experiment: Alter number of reference points.",
      experimentValueName = Experiment.refPoints,
      values,
      Seq[Seq[AlgorithmResult]](),
      writeName = "2. Reference Points"
    )

    experimentResult.write()

    for(setup <- experimentSetups) {
      experimentResult.algorithmResultsForEachValue :+=
        Seq[AlgorithmResult](
          runExperimentEmbedded(setup)
        )
      experimentResult.write()
    }
  }

  /**
   * 3. Alter vertices
   */
  def expVertices(runs: Int, shortExperiments: Boolean) = {
    var values = Seq(10, 100, 1000, 10000, 50000, 100000, 200000, 500000)
    if (shortExperiments) values = values.dropRight(3)

    val experimentSetups = values map { value =>
      ExperimentSetup(
        experimentValueName = Experiment.vertices,
        experimentValue = value,
        approximateVertices = value,
        runs = runs
      )
    }

    val experimentResult: ExperimentResult = new ExperimentResult(
      experimentTitle = "3. Experiment: Alter number of vertices.",
      experimentValueName = Experiment.vertices,
      values,
      Seq[Seq[AlgorithmResult]](),
      writeName = "3. Vertices"
    )

    experimentResult.write()

    for(setup <- experimentSetups) {
      experimentResult.algorithmResultsForEachValue :+=
        Seq[AlgorithmResult](
          runExperimentNaive(setup),
          runExperimentEager(setup),
          runExperimentEmbedded(setup)
        )
      experimentResult.write()
    }
  }

  /**
   * 4. Alter object density
   */
  def expObjectDensity(runs: Int, shortExperiments: Boolean) = {
    var values = Seq(0.005, 0.01, 0.02, 0.04, 0.08, 0.16, 0.32, 0.64, 1.0)
    if (shortExperiments) values = values.drop(2).dropRight(3)

    val experimentSetups = values map { value =>
      ExperimentSetup(
        experimentValueName = Experiment.objectDensity,
        experimentValue = value,
        objectDensity = value,
        runs = runs
      )
    }

    val experimentResult: ExperimentResult = new ExperimentResult(
      experimentTitle = "4. Experiment: Alter object density.",
      experimentValueName = Experiment.objectDensity,
      values,
      Seq[Seq[AlgorithmResult]](),
      writeName = "4. Objects"
    )

    experimentResult.write()

    for(setup <- experimentSetups) {
      experimentResult.algorithmResultsForEachValue :+=
        Seq[AlgorithmResult](
          runExperimentNaive(setup),
          runExperimentEager(setup),
          runExperimentEmbedded(setup)
        )
      experimentResult.write()
    }
  }

  /**
   * 5. Alter Connectivity (Edges)
   */
  def expConnectivity(runs: Int, shortExperiments: Boolean) = {
    var values = Seq(0.0, 0.005, 0.01, 0.02, 0.04, 0.08, 0.16, 0.32, 0.64, 1.0)
    if (shortExperiments) values = values.drop(2).dropRight(3)

    val experimentSetups = values map { value =>
      ExperimentSetup(
        experimentValueName = Experiment.connectivity,
        experimentValue = value,
        connectivity = value,
        runs = runs
      )
    }

    val experimentResult: ExperimentResult = new ExperimentResult(
      experimentTitle = "5. Experiment: Alter connectivity (number of edges).",
      experimentValueName = Experiment.connectivity,
      values,
      Seq[Seq[AlgorithmResult]](),
      writeName = "5. Edges"
    )

    experimentResult.write()

    for(setup <- experimentSetups) {
      experimentResult.algorithmResultsForEachValue :+=
        Seq[AlgorithmResult](
          runExperimentNaive(setup),
          runExperimentEager(setup),
          runExperimentEmbedded(setup)
        )
      experimentResult.write()
    }
  }

  /**
   * 6. Alter k
   */
  def expK(runs: Int, shortExperiments: Boolean) = {
    var values = Seq(1, 2, 4, 8, 16)
    if (shortExperiments) values = values.drop(1).dropRight(1)

    val experimentSetups = values map { value =>
      ExperimentSetup(
        experimentValueName = Experiment.k,
        experimentValue = value,
        k = value,
        runs = runs
      )
    }

    val experimentResult: ExperimentResult = new ExperimentResult(
      experimentTitle = "6. Experiment: Alter k.",
      experimentValueName = Experiment.k,
      values,
      Seq[Seq[AlgorithmResult]](),
      writeName = "6. k"
    )

    experimentResult.write()

    for(setup <- experimentSetups) {
      experimentResult.algorithmResultsForEachValue :+=
        Seq[AlgorithmResult](
          runExperimentNaive(setup),
          runExperimentEager(setup),
          runExperimentEmbedded(setup)
        )
      experimentResult.write()
    }
  }


  /**
   * For each graph in the given setup, run the naive rknn algorithm.
   * @param setup
   * @return AlgorithmResult
   */
  def runExperimentNaive(setup: ExperimentSetup): AlgorithmResult = {
    var nodesToRefine    = Seq[Int]()
    var nodesVisited     = Seq[Int]()
    var runTimeRknnQuery = Seq[Int]()

    for{
      (sGraph, q) <- setup.sGraphsQIds
    }
    yield {
      Log.resetStats()

      naiveRkNN(sGraph, q, k = setup.k)
      nodesToRefine    :+= (Log.nodesToVerify)   .toInt
      nodesVisited     :+= (Log.nodesVisited)    .toInt
      runTimeRknnQuery :+= (Log.runTimeRknnQuery).toInt
    }

    val naiveSingleResults = Seq[SingleResult](
      new SingleResult("Candidates to refine on graph"      , nodesToRefine),
      new SingleResult("Nodes visited"                      , nodesVisited),
      new SingleResult("Runtime thread CPU rknn query (ms.)", runTimeRknnQuery)
    )

    new AlgorithmResult("Naive", setup.runs, setup.experimentValueName, setup.experimentValue, naiveSingleResults)
  }

  /**
   * For each graph in the given setup, run the eager rknn algorithm.
   * @param setup
   * @return AlgorithmResult
   */
  def runExperimentEager(setup: ExperimentSetup): AlgorithmResult = {
    var nodesToRefine    = Seq[Int]()
    var nodesVisited     = Seq[Int]()
    var runTimeRknnQuery = Seq[Int]()

    for{
      (sGraph, q) <- setup.sGraphsQIds
    }
    yield {
      Log.resetStats()

      eagerRkNN(sGraph, q, k = setup.k)
      nodesToRefine    :+= (Log.nodesToVerify)   .toInt
      nodesVisited     :+= (Log.nodesVisited)    .toInt
      runTimeRknnQuery :+= (Log.runTimeRknnQuery).toInt
    }

    val eagerSingleResults = Seq[SingleResult](
      new SingleResult("Candidates to refine on graph"      , nodesToRefine),
      new SingleResult("Nodes visited"                      , nodesVisited),
      new SingleResult("Runtime thread CPU rknn query (ms.)", runTimeRknnQuery)
    )

    new AlgorithmResult("Eager", setup.runs, setup.experimentValueName, setup.experimentValue, eagerSingleResults)
  }

  /**
   * For each graph in the given setup, run the embedded rknn algorithm.
   * @param setup
   * @return AlgorithmResult
   */
  def runExperimentEmbedded(setup:ExperimentSetup): AlgorithmResult = {
    var nodesToRefine      = Seq[Int]()
    var nodesVisited       = Seq[Int]()
    var runTimeRknnQuery   = Seq[Int]()

    var filteredCandidates = Seq[Int]()
    var runTimePreparation = Seq[Int]()

    for{
      (sGraph, q) <- setup.sGraphsQIds
    }
    yield {
      Log.resetStats()

      embeddedRkNN(sGraph, q, k = setup.k, numRefPoints = setup.numRefPoints, rStarTreePageSize = setup.rStarTreePageSize)
      nodesToRefine      :+= (Log.nodesToVerify)   .toInt
      nodesVisited       :+= (Log.nodesVisited)    .toInt
      runTimeRknnQuery   :+= (Log.runTimeRknnQuery).toInt

      runTimePreparation :+= (Log.embeddingRunTimePreparation).toInt
      filteredCandidates :+= (Log.embeddingFilteredCandidates).toInt
    }

    val embeddedSingleResults = Seq[SingleResult](
      new SingleResult("Candidates to refine on graph"      , nodesToRefine),
      new SingleResult("Nodes visited"                      , nodesVisited),
      new SingleResult("Runtime thread CPU rknn query (ms.)", runTimeRknnQuery),
      new SingleResult("Candidates left after filter"       , filteredCandidates),
      new SingleResult("Runtime embedding preparation (ms.)", runTimeRknnQuery)
    )

    new AlgorithmResult("Embedded", setup.runs, setup.experimentValueName, setup.experimentValue, embeddedSingleResults)
  }

  def naiveRkNN(sGraph: SGraph, q: SVertex, k: Int) : Unit = {

    Log.appendln(s"\n-----------Naive R${k}NN for query point ${q.id}:-----------\n")

    val timeNaiveRkNN  = ThreadCPUTimeDiff()

    val rkNNsNaive     = naiveRkNNs(sGraph, q, k)

    timeNaiveRkNN.end
    Log.runTimeRknnQuery = timeNaiveRkNN.diffMillis

    Log.appendln(s"Result r${k}NNs: ${if (rkNNsNaive.size == 0) "--" else ""}")
    for( v <- rkNNsNaive )
      Log.appendln(s"Node: ${v._1.id}  Dist: ${v._2}")

    Log.appendln(s"\nTotal Simple RkNN Runtime: $timeNaiveRkNN \n")
    Log.printFlush
  }

  def eagerRkNN(sGraph: SGraph, q: SVertex, k: Int) : Unit = {

    Log.appendln(s"-----------Eager R${k}NN for query point ${q.id}:-----------\n")

    val timeEagerRkNN  = ThreadCPUTimeDiff()

    val rkNNsEager     = eager(sGraph, q, k)

    timeEagerRkNN.end
    Log.runTimeRknnQuery = timeEagerRkNN.diffMillis

    Log.appendln(s"Result r${k}NNs: ${if (rkNNsEager.size == 0) "--" else ""}")
    for( v <- rkNNsEager )
      Log.appendln(s"Node: ${v._1.id}  Dist: ${v._2}")

    Log.appendln(s"\nTotal Eager RkNN Runtime: $timeEagerRkNN \n")
  }

  def embeddedRkNN(sGraph: SGraph, q: SVertex, k: Int, numRefPoints: Int, rStarTreePageSize: Int) : Unit = {

    Log.appendln(s"-----------Embedded R${k}NN for query point ${q.id}:-----------\n")
    Log.printFlush

    val timeEmbeddedRkNN = ThreadCPUTimeDiff()

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
    println("Code organization phase: Performing dry run (running all algorithms once)")

    val setup = new ExperimentSetup(
      experimentValueName = null,
      experimentValue     = -1.0,
      approximateVertices = 1000,
      objectDensity       = 0.05,
      connectivity        = 0.1,
      k                   = 3,
      numRefPoints        = 3,
      entriesPerNode      = 25,
      runs                = 1
    )

    val (sGraph, q) = setup.sGraphsQIds.head

    naiveRkNN(sGraph, q, k = setup.k)
    eagerRkNN(sGraph, q, k = setup.k)
    embeddedRkNN(sGraph, q, k = setup.k, numRefPoints = setup.numRefPoints, rStarTreePageSize = setup.rStarTreePageSize)

    println("Dry run completed. Now starting experiments.\n\n")
  }
}