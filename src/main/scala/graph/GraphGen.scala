package graph

import graph.GraphGenerator
import scala.util.Random
import graph.core.Edge

import util.Utils.makesure

/**
 * Created: 04.02.14, 16:42
 * @author fliebhart
 */
object GraphGen {

  def generateJavaGraph(numberOfVertices: Integer, numberOfEdges: Integer, numberOfObjects: Integer, weightOne: Boolean): graph.core.Graph = {
    makesure(numberOfVertices > 0,                                        s"Number of vertices ($numberOfVertices) must be > 0")
    makesure(numberOfObjects >= 0 && numberOfObjects <= numberOfVertices, s"Number of objects ($numberOfObjects) must be >= 0 and <= number of vertices ($numberOfVertices)")
    makesure(numberOfEdges > numberOfVertices - 1,                        s"number of edges must be > ${numberOfVertices-1} (number of vertices - 1).")
    val max: Integer = ((numberOfVertices.toDouble * (numberOfVertices.toDouble - 1)) / 2).toInt
    makesure(numberOfEdges <= max,                                        s"Number of edges ($numberOfEdges) must be <= $max for ${numberOfVertices} vertices")

    println("Generating graph with " + numberOfVertices + " nodes, " + numberOfEdges + " edges, " + numberOfObjects + " objects..." )
    val t0 = System.currentTimeMillis()
    GraphGenerator.getInstance().generateProbabilisticGraph(numberOfVertices, numberOfEdges, 0, numberOfObjects, weightOne)
    val t1 = System.currentTimeMillis()
    println("Graph generation finished. Runtime: " + (t1-t0) + " ms.")

    ProbabilisticGraph.getInstance()

//    val graph = new SGraph
//
//    // create vertices
//    (0 to numberOfVertices-1) map { id => graph.addVertex(new SVertex(id)) }
//
//    // create random objects
//    val objectIds = Random.shuffle((0 to numberOfObjects-1) ++ List.fill(numberOfVertices-numberOfObjects)(-1))
//    (graph.getAllVertices zip objectIds) map { case (v, id) if id != -1 => v.setObjectId(id) }
//
//    // create edges
//    val shuffledVertices = Random.shuffle(graph.getAllVertices)
//    (0 to numberOfEdges-1) map { x =>
//      shuffledVertices
//
//    }
//    val e41 = new Edge(n4, n1)
//    e41.setWeight(1)
//    graph.addEdge() (e41)
  }

  def generateScalaGraph(numberOfVertices: Integer, numberOfEdges: Integer, numberOfObjects: Integer, weightOne: Boolean): SGraph = {
    val jGraph = generateJavaGraph(numberOfVertices, numberOfEdges, numberOfObjects, weightOne)
    util.Utils.convertJavaToScalaGraph(jGraph)
  }
}
