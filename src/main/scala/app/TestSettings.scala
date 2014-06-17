package app

import scala.util.Random
import graph.GraphGen
import util.XmlUtil
import algorithms.Embedding
import util.Utils._
import TestSettings._


object TestSettings {
  val defaultVertices       = 75000
  val defaultObjectDensity  = 0.05 // every 20th
  val defaultConnectivity   = 0.1
  val defaultK              = 3
  val defaultNumRefPoints   = 6
  val defaultEntriesPerNode = 25
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
case class TestSettings(var approximateVertices: Int, var objectDensity: Double, var connectivity: Double, var k: Int, var numRefPoints: Int, var entriesPerNode: Int) {

  def this() = {
    this(
      defaultVertices,
      defaultObjectDensity,
      defaultConnectivity,
      defaultK,
      defaultNumRefPoints,
      defaultEntriesPerNode
    )
  }

  def vertices          = Math.pow(Math.sqrt(approximateVertices).floor, 2).toInt
  def objects           = if (objectDensity * vertices == 0) 1 else (objectDensity * vertices).ceil.toInt

  def nrOfRowsAndCols   = Math.sqrt(vertices)
  def rowEdges          = nrOfRowsAndCols * (nrOfRowsAndCols - 1)
  def minEdges          = rowEdges + (nrOfRowsAndCols - 1)
  def maxEdges          = (nrOfRowsAndCols - 1) * 2 * nrOfRowsAndCols // Maximum: for n rows: 2n edges.    // (Math.pow(nrOfRowsAndCols, 2))    // Maximum Edges: all edges between all rows: cols * (rows - 1)
  def edges             = (connectivity * (maxEdges - minEdges) + minEdges).toInt   // generally for a graph: from N-1 to N(N-1)/2 // Int Overflow at: max 2.147.483.647 => Vertex max: 65.536

  var qID               = -1
  def rStarTreePageSize = (entriesPerNode * numRefPoints * 16) + 34


  def default() = {
    approximateVertices = defaultVertices
    objectDensity       = defaultObjectDensity
    connectivity        = defaultConnectivity
    k                   = defaultK
    numRefPoints        = defaultNumRefPoints
    entriesPerNode      = defaultEntriesPerNode
  }

  override def toString() = {
    s"""Object density:          $objectDensity
       |Vertices:                $approximateVertices
       |Connectivity:            $connectivity
       |k:                       $k
       |Reference Points:        $numRefPoints
       |Entries per r*tree node: $entriesPerNode""".stripMargin
  }
}
