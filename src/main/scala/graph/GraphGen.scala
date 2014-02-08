package graph

import graph.GraphGenerator

/**
 * Created: 04.02.14, 16:42
 * @author fliebhart
 */
object GraphGen {

  def generateJavaGraph(numberOfVertices: Integer, numberOfEdges: Integer, numberOfObjects: Integer, weightOne: Boolean): graph.core.Graph = {
    require(numberOfVertices > 0)
    require(numberOfObjects >= 0 && numberOfObjects <= numberOfVertices)
    require(numberOfEdges > 0)
    val max: Integer = (numberOfVertices * numberOfVertices - 1) / 2
    require(numberOfEdges <= max)

    val t0 = System.currentTimeMillis()
    GraphGenerator.getInstance().generateProbabilisticGraph(numberOfVertices, numberOfEdges, 0, numberOfObjects, weightOne)
    val t1 = System.currentTimeMillis()

    println("Runtime: " + (t1-t0)/1000.0 + " sec. for generating Graph with " + numberOfVertices + " nodes, " + numberOfEdges + " edges, " + numberOfObjects + " objects." )

    ProbabilisticGraph.getInstance()
//    val graph = new SGraph
//
//    // create vertices
//    (0 to numberOfVertices-1) map { id => graph.addVertex(new SVertex(id)) }
//
//    // create random objects
//    val objectIds = Random.shuffle((0 to numberOfObjects-1).toIndexedSeq ++ List.fill(numberOfVertices-numberOfObjects)(-1))
//    (graph.getAllVertices zip objectIds) map { case (v, id) if id != -1 => v.setObjectId(id) }
//
//    // create edges
//    val e41 = new Edge(n4, n1)
//    e41.setWeight(5)
//    graph.addEdge(e41)
  }

  def generateScalaGraph(numberOfVertices: Integer, numberOfEdges: Integer, numberOfObjects: Integer, weightOne: Boolean): SGraph = {
    val jGraph = generateJavaGraph(numberOfVertices, numberOfEdges, numberOfObjects, weightOne)
    util.Utils.convertJavaToScalaGraph(jGraph)
  }
}
