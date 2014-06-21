package app

import scala.util.Random
import graph.{SVertex, SGraph, GraphGen}
import ExperimentSetup._
import scala.collection.immutable.HashMap
import app.Experiment.Experiment


object ExperimentSetup {
  val defaultVertices       = 75000 
  val defaultObjectDensity  = 0.05 // every 20th
  val defaultConnectivity   = 0.3
  val defaultK              = 3
  val defaultNumRefPoints   = 15
  val defaultEntriesPerNode = 25
  val defaultRuns           = 5

  implicit def double2Int(d: Double) = d.toInt
}

/**
 *
 * @param vertices        // e.g. 1000        Max. 1 Million! (for no integeroverflow for max-edges)
 * @param objectDensity   // recom.: 0.05
 * @param connectivity    // recom.: 0.1
 * @param k               // e.g. 4
 * @param numRefPoints    // e.g. 6
 * @param entriesPerNode  // recom.: 25
 */
case class ExperimentSetup(experiment         : Experiment       = Experiment.Default,
                           entriesPerNode     : Int              = defaultEntriesPerNode,
                           runs               : Int              = defaultRuns,
                           numRefPoints       : Int              = defaultNumRefPoints,
                           approximateVertices: Int              = defaultVertices,
                           objectDensity      : Double           = defaultObjectDensity,
                           connectivity       : Double           = defaultConnectivity,
                           k                  : Int              = defaultK){

  def apply(experiment: Experiment, experimentValue: Double) {
    experiment match {
      case Experiment.Default        => ExperimentSetup(experiment = experiment)
      case Experiment.EntriesPerNode => ExperimentSetup(experiment = experiment, entriesPerNode      = experimentValue)
      case Experiment.RefPoints      => ExperimentSetup(experiment = experiment, numRefPoints        = experimentValue)
      case Experiment.Vertices       => ExperimentSetup(experiment = experiment, approximateVertices = experimentValue)
      case Experiment.ObjectDensity  => ExperimentSetup(experiment = experiment, objectDensity       = experimentValue)
      case Experiment.Connectivity   => ExperimentSetup(experiment = experiment, connectivity        = experimentValue)
      case Experiment.K              => ExperimentSetup(experiment = experiment, k                   = experimentValue)
    }
  }

  val vertices              = Math.pow(Math.sqrt(approximateVertices).floor, 2).toInt
  val objects               = if (objectDensity * vertices <= 1) 2 else (objectDensity * vertices).ceil.toInt
  val nrOfRowsAndCols       = Math.sqrt(vertices)
  val rowEdges              = nrOfRowsAndCols * (nrOfRowsAndCols - 1)
  val minEdges              = rowEdges + (nrOfRowsAndCols - 1)
  val maxEdges              = (nrOfRowsAndCols - 1) * 2 * nrOfRowsAndCols // Maximum: for n rows: 2n edges.    // (Math.pow(nrOfRowsAndCols, 2))    // Maximum Edges: all edges between all rows: cols * (rows - 1)
  val edges                 = (connectivity * (maxEdges - minEdges) + minEdges).toInt   // generally for a graph: from N-1 to N(N-1)/2 // Int Overflow at: max 2.147.483.647 => Vertex max: 65.536
  val rStarTreePageSize     = (entriesPerNode * numRefPoints * 16) + 34

  val sGraphsQIds: Seq[(SGraph, SVertex)] =
    for {_ <- 1 to runs} yield {
      val sGraph = GraphGen.generateScalaGraph(vertices, edges, objects)
      val q      = sGraph.getVertex(new Random(System.currentTimeMillis).nextInt(vertices))
      if (!q.containsObject)
        q.setObjectId(vertices)
      (sGraph, q)
  }

  override def toString = {
    (HashMap[Experiment, String](
      Experiment.ObjectDensity  -> s"Object Density:          $objectDensity",
      Experiment.Vertices       -> s"Vertices:                $approximateVertices",
      Experiment.Connectivity   -> s"Connectivity:            $connectivity",
      Experiment.K              -> s"k:                       $k",
      Experiment.RefPoints      -> s"Reference points:        $numRefPoints",
      Experiment.EntriesPerNode -> s"Entries per R*Tree node: $entriesPerNode") - experiment).values mkString ("\n")
  }
}
