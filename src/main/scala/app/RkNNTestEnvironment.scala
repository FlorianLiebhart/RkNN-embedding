package app

import java.util.Date
import java.text.SimpleDateFormat

import app.Experiment.Experiment
import algorithms.{Eager, Naive, GraphRknn, Embedding}
import util.Log

object RkNNTestEnvironment {

  def main(args: Array[String]): Unit = {

    val date = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date())
    Log.appendln(s"###### Start: $date ######\n")
    Log.writeFlushWriteLog(appendToFile = false)

    try {
      runExperiments(
        short = false,
        runs  = 5
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

  def runExperiments(runs: Int, short: Boolean) {
    dryRun()

    println("--------------- Starting experiments. -----------------\n")
    
    val naive     = Naive
    val eager     = Eager
    val embedding = Embedding
    val algorithms = Seq(naive, eager, embedding)

    runExperiment(Experiment.Default       , algorithms,     runs, short, Seq(),                                                     Seq())
    runExperiment(Experiment.EntriesPerNode, Seq(embedding), runs, short, Seq(5, 10, 15, 20, 25, 30, 35, 40, 45, 50),                Seq(5, 10, 15, 20))
    runExperiment(Experiment.RefPoints     , Seq(embedding), runs, short, Seq(5, 10, 15, 20, 25, 30, 35, 40, 45, 50),                Seq(5, 10, 15, 20))
    runExperiment(Experiment.Vertices      , algorithms,     runs, short, Seq(10, 100, 1000, 10000, 50000, 100000, 200000, 500000),  Seq(10,100,1000,10000))
    runExperiment(Experiment.ObjectDensity , algorithms,     runs, short, Seq(0.005, 0.01, 0.02, 0.04, 0.08, 0.16, 0.32, 0.64, 1.0), Seq(0.02, 0.04, 0.08, 0.16))
    runExperiment(Experiment.Connectivity  , algorithms,     runs, short, Seq(0.005, 0.01, 0.02, 0.04, 0.08, 0.16, 0.32, 0.64, 1.0), Seq(0.02, 0.04, 0.08, 0.16))
    runExperiment(Experiment.K             , algorithms,     runs, short, Seq(1, 2, 4, 8, 16),                                       Seq(2, 4, 8))

    println("\n------------- All experiments finished. ---------------\n")
  }


  def runExperiment(experiment: Experiment, algorithms: Seq[GraphRknn], runs: Int, short: Boolean, expValues: Seq[Double], shortExpValues: Seq[Double]) = {
    val values = if(short) shortExpValues else expValues

    val setups =
      if(values.isEmpty)
        Seq(ExperimentSetup())
      else values map { value =>
      ExperimentSetup(
        experiment = experiment,
        runs = runs
      )
    }

    val experimentResult: ExperimentResult = new ExperimentResult(
      experiment                   = experiment,
      values                       = values,
      algorithmResultsForEachValue = Seq[Seq[AlgorithmResult]]()
    )

    experimentResult.write()

    for(setup <- setups){
      val algorithmResults: Seq[AlgorithmResult] = algorithms map (runExperimentForAlgorithm(setup, _))

      experimentResult.algorithmResultsForEachValue :+= algorithmResults
      experimentResult.write()
    }
  }


  /**
   * For each graph in the given setup, run the given rknn algorithm
   * @param setup
   * @return AlgorithmResult
   */
  def runExperimentForAlgorithm(setup: ExperimentSetup, algorithm: GraphRknn): AlgorithmResult = {
    var nodesToRefine      = Seq[Int]()
    var nodesVisited       = Seq[Int]()
    var runTimeRknnQuery   = Seq[Int]()

    var embeddingFilteredCandidates = Seq[Int]()
    var embeddingRunTimePreparation = Seq[Int]()

    for((sGraph, q) <- setup.sGraphsQIds) {
      Log.resetStats()

      algorithm match {
        case Naive     => Naive.rknns(sGraph, q, setup.k)
        case Eager     => Eager.rknns(sGraph, q, setup.k)
        case Embedding => val (relation, rStarTree, dbidVertexIDMapping) = Embedding.createDatabaseWithIndex(sGraph, setup.numRefPoints, setup.rStarTreePageSize)
                          val queryObject                                = Embedding.getQueryObject(relation, q, dbidVertexIDMapping)
                          Embedding.rknns(sGraph, q, setup.k, relation, queryObject, rStarTree, dbidVertexIDMapping)
      }
  Integer.MAX_VALUE
      Int.MaxValue
      nodesToRefine    :+= Log.nodesToVerify
      nodesVisited     :+= Log.nodesVisited
      runTimeRknnQuery :+= Log.runTimeRknnQuery

      embeddingFilteredCandidates :+= Log.embeddingFilteredCandidates
      embeddingRunTimePreparation :+= Log.embeddingRunTimePreparation
    }

    val singleResults = Seq[SingleResult](
      new SingleResult("Candidates to refine on graph"      , nodesToRefine),
      new SingleResult("Nodes visited"                      , nodesVisited),
      new SingleResult("Runtime thread CPU rknn query (ms.)", runTimeRknnQuery)
    ) ++ (
      algorithm match {
        case Embedding => Seq[SingleResult](
                            new SingleResult("Candidates left after filter"       , embeddingFilteredCandidates),
                            new SingleResult("Runtime embedding preparation (ms.)", embeddingRunTimePreparation)
                          )
        case _         => Nil
      }
    )

    new AlgorithmResult(algorithm.name, setup.runs, setup.experiment, singleResults)
  }

  /**
   *  Code-Organization: Perform all algorithms prior running the tests, so that the JVM can organize code.
   */
  def dryRun() = {
    println("Code organization phase: Performing dry run (running all algorithms once)")

    val setup = new ExperimentSetup(
      experiment          = null,
      approximateVertices = 1000,
      objectDensity       = 0.05,
      connectivity        = 0.1,
      k                   = 3,
      numRefPoints        = 3,
      entriesPerNode      = 25,
      runs                = 1
    )

    val (sGraph, q) = setup.sGraphsQIds.head

    Naive.rknns(sGraph, q, setup.k)
    Eager.rknns(sGraph, q, setup.k)
    val (relation, rStarTree, dbidVertexIDMapping) = Embedding.createDatabaseWithIndex(sGraph, setup.numRefPoints, setup.rStarTreePageSize)
    val queryObject                                = Embedding.getQueryObject(relation, q, dbidVertexIDMapping)
    Embedding.rknns(sGraph, q, setup.k, relation, queryObject, rStarTree, dbidVertexIDMapping)

    println("Dry run completed. Now starting experiments.\n\n")
  }
}