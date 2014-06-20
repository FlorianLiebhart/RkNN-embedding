package app

import scala.util.Random
import graph.{SVertex, SGraph, GraphGen}
import util.XmlUtil
import algorithms.Embedding
import util.Utils._
import ExperimentSetup._
import scala.collection.immutable.HashMap


object ExperimentSetup {
  val defaultVertices       = 7500 // TODO CHANGE BACK to 75000!!!!
  val defaultObjectDensity  = 0.05 // every 20th
  val defaultConnectivity   = 0.1
  val defaultK              = 3
  val defaultNumRefPoints   = 6
  val defaultEntriesPerNode = 25
  val defaultRuns           = 5
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
case class ExperimentSetup(experimentValueName: Experiment.Value = Experiment.default,
                           experimentValue    : Double           = -1.0,
                           approximateVertices: Int              = defaultVertices,
                           objectDensity      : Double           = defaultObjectDensity,
                           connectivity       : Double           = defaultConnectivity,
                           k                  : Int              = defaultK,
                           numRefPoints       : Int              = defaultNumRefPoints,
                           entriesPerNode     : Int              = defaultEntriesPerNode,
                           runs               : Int              = defaultRuns){

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
      val qID    =sGraph.getVertex(new Random(System.currentTimeMillis).nextInt(vertices))
      (sGraph, qID)
  }


  override def toString = {
    (HashMap[Experiment.Value, String](
      Experiment.objectDensity  -> s"Object Density:          $objectDensity",
      Experiment.vertices       -> s"Vertices:                $approximateVertices",
      Experiment.connectivity   -> s"Connectivity:            $connectivity",
      Experiment.k              -> s"k:                       $k",
      Experiment.refPoints      -> s"Reference points:        $numRefPoints",
      Experiment.entriesPerNode -> s"Entries per R*Tree node: $entriesPerNode") - experimentValueName).values mkString ("\n")
  }
}
