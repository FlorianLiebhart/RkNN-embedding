package graph

import graph.GraphGenerator
import scala.util.Random
import graph.core.Edge

/**
 * Created: 04.02.14, 16:42
 * @author fliebhart
 */
object GraphGen {

  def generateJavaGraph(numberOfVertices: Integer, numberOfEdges: Integer, numberOfObjects: Integer, weightOne: Boolean): graph.core.Graph = {
    require(numberOfVertices > 0)
    require(numberOfObjects >= 0 && numberOfObjects <= numberOfVertices)
    require(numberOfEdges > 0)
    val max: Integer = ((numberOfVertices.toDouble * (numberOfVertices.toDouble - 1)) / 2).toInt
    require(numberOfEdges <= max)

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
