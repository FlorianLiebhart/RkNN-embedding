package graph

import scala.util.Random
import scala.collection.JavaConversions._

import util.Utils.makesure
import util.CPUTimeDiff
import util.GuiConstants
import util.Log
import graph.core.Graph

object GraphGen {

  def generateJavaGraph(numberOfVertices: Integer, numberOfEdges: Integer, numberOfObjects: Integer, weightOne: Boolean): graph.core.Graph = {
    checkPreconditions(numberOfVertices,numberOfEdges,numberOfObjects)

    Log.appendln("Generating graph with " + numberOfVertices + " nodes, " + numberOfEdges + " edges, " + numberOfObjects + " objects..." )
    val timeGenerateGraph = CPUTimeDiff()

    GraphGenerator.getInstance().generateProbabilisticGraph(numberOfVertices, numberOfEdges, 0, numberOfObjects, weightOne)

    timeGenerateGraph.end
    Log.appendln(s"Graph generation finished. Runtime: $timeGenerateGraph")

    ProbabilisticGraph.getInstance()
  }

  def generateScalaGraph(numberOfVertices: Integer, numberOfEdges: Integer, numberOfObjects: Integer, edgeMaxWeight: Int = 10): SGraph = {
    checkPreconditions(numberOfVertices, numberOfEdges, numberOfObjects)

    Log.appendln("Generating graph with " + numberOfVertices + " nodes, " + numberOfEdges + " edges, " + numberOfObjects + " objects..." )
    val timeGenerateGraph = CPUTimeDiff()


    val sGraph = new SGraph

    /*
     * create vertices
     */
    Log.append(s"  - Creating $numberOfVertices vertices...")
    val timeCreateVertices = CPUTimeDiff()

    (0 to numberOfVertices - 1) map { id => sGraph.addVertex(new SVertex(id)) }

    timeCreateVertices.end
    Log.appendln(s" done in $timeCreateVertices")
    /*
     * create random objects
     */
    Log.append(s"  - Creating $numberOfObjects random objects...")
    val timeCreateObjects = CPUTimeDiff()

    val shuffledVertices = new Random(System.currentTimeMillis).shuffle(sGraph.getAllVertices)
    val objectIds        = (0 to numberOfObjects - 1)
    (shuffledVertices zip objectIds) map { case (v, id) => v.setObjectId(id) }

    timeCreateObjects.end
    Log.appendln(s" done in $timeCreateObjects")

    /*
     * create node positions
     */
    Log.append(s"  - Creating $numberOfVertices node positions...")
    val timeCreateNodePositions = CPUTimeDiff()

    val nrOfRows = setNodesPosition(sGraph)

    timeCreateNodePositions.end
    Log.appendln(s" done in $timeCreateNodePositions")

    /*
     * create random edges
     */
    Log.append(s"  - Creating $numberOfEdges random edges...")
    val timeCreateEdges = CPUTimeDiff()

    // create edges
    createRandomEdges(sGraph, numberOfEdges, nrOfRows)

    // create edge weights
    createRandomEdgeWeights(sGraph.getAllEdges, edgeMaxWeight)

    timeCreateEdges.end
    Log.appendln(s" done in $timeCreateEdges")

    timeGenerateGraph.end
    Log.appendln(s"Graph generation finished. Runtime: $timeGenerateGraph\n")

    sGraph
  }

  def createRandomEdges(sGraph: SGraph, numberOfEdges: Integer, numberOfRows: Integer) = {

    // create row connections: connect all neighboring vertices within each row
    val vertexColumns = sGraph.getAllVertices.grouped(numberOfRows).toList    // Math.ceil(numberOfNodes / nrOfRows)  (e.g. Math.ceil(1000 / 7) = 143)

    for(vertexColumn <- vertexColumns) {
      var lastVertex = vertexColumn.head
      for(currentVertex <- vertexColumn.tail) {
        sGraph.addEdge(new SEdge(lastVertex, currentVertex))
        lastVertex = currentVertex
      }
    }

    // create column connections: random connections between rows
    val numberOfColumns            = vertexColumns.size
    val currentNumberOfEdges       = sGraph.getAllEdges.size
    val availableColumnConnections = numberOfEdges - currentNumberOfEdges
    val edgesBetweenEachColumnPair = availableColumnConnections / (numberOfColumns - 1)

    var lastCol        = vertexColumns.head
    var lastTimeMillis = System.currentTimeMillis
    for(currentCol <- vertexColumns.tail) {
      val currentTimeMilis = System.currentTimeMillis
      val shuffledCurrentCol = new Random(currentTimeMilis + lastTimeMillis).shuffle(currentCol)
      lastTimeMillis = currentTimeMilis
      val verticesToConnect  = (lastCol zip shuffledCurrentCol).take(edgesBetweenEachColumnPair)

      for(vertexTuple <- verticesToConnect){
        sGraph.addEdge(new SEdge(vertexTuple._1, vertexTuple._2))
      }
      lastCol = shuffledCurrentCol
    }
  }


  /**
	 * Set random weight for all edges between 1 and upperWeightLimit
	 */
	def createRandomEdgeWeights(edges: Seq[SEdge], upperWeightLimit: Int) {
		val randomer = new Random(System.currentTimeMillis)
    edges map { e =>
			e.setWeight(randomer.nextInt(upperWeightLimit - 1) + 1)
    }
	}

  /**
   * Create nodes' positions so that it comes closest to a street network
   * . . . . . . . . . . . . . . . . . .
   * . . . . . . . . . . . . . . . . . .
   * . . . . . . . . . . . . . . . . . .
   * . . . . . . . . . . . . . . . . . .
   * . . . . . . . . . . . . . . . . . .
   * . . . . . . . . . . . . . . . . . .
   *
   * @return Number of rows
   */
  def setNodesPosition(sGraph: SGraph): Int = {
    val numberOfNodes = sGraph.getAllVertices.size

//    val MAX_ROWS = 7
//    var rowCount = (numberOfNodes / 6) + 1
//
//    if(rowCount == 1)
//      rowCount = 2 // at least 2 rows
//    else if(rowCount > MAX_ROWS)
//      rowCount = MAX_ROWS

    val rowCount = Math.sqrt(numberOfNodes).ceil.toInt

    var xPos       = GuiConstants.X_INIT_POS; // initial position 75
    var yPos       = GuiConstants.Y_INIT_POS  // initial position 75
    var currentRow = 0
    for(v <- sGraph.getAllVertices) {
      if(currentRow != 0 && currentRow % rowCount == 0) {
        xPos += GuiConstants.X_DISTANCE; // back to top 150
        yPos =  GuiConstants.Y_INIT_POS  // 75
        currentRow = 0
      }
      if(currentRow != 0) {
        yPos = yPos + GuiConstants.Y_DISTANCE  // 150
      }

      v.setNodeLocation(xPos, yPos)
      currentRow += 1
    }

    rowCount
  }

  def checkPreconditions(numberOfVertices: Integer, numberOfEdges: Integer, numberOfObjects: Integer) {
    makesure(numberOfVertices > 0,                                        s"Number of vertices ($numberOfVertices) must be > 0")
    makesure(numberOfObjects > 1 && numberOfObjects <= numberOfVertices,  s"Number of objects ($numberOfObjects) must be >= 0 and <= number of vertices ($numberOfVertices)")
    makesure(numberOfEdges >= numberOfVertices - 1,                       s"number of edges ($numberOfEdges) must be >= number of vertices (${numberOfVertices-1}) - 1.")
    val max: Integer = ((numberOfVertices.toDouble * (numberOfVertices.toDouble - 1)) / 2).toInt
    makesure(numberOfEdges <= max,                                        s"Number of edges ($numberOfEdges) must be <= $max for ${numberOfVertices} vertices")
  }


  def convertScalaToJavaGraph(sGraph: SGraph): graph.core.Graph = {
    Log.append("\nConverting SGraph to Java graph with " + sGraph.getAllVertices.size + " nodes, " + sGraph.getAllEdges.size + " edges..")

    val timeConvertScalaToJavaGraph = CPUTimeDiff()

    val jGraph = new graph.core.Graph()
    sGraph.getAllVertices.map(v => jGraph.addVertex(convertScalaToJavaVertex(v)))
    sGraph.getAllEdges   .map(e => jGraph.addEdge  (convertScalaToJavaEdge(jGraph, e)))

    timeConvertScalaToJavaGraph.end
    Log.appendln(s" done in $timeConvertScalaToJavaGraph\n")

    jGraph
  }

  def convertJavaToScalaGraph(jGraph: Graph): SGraph = {
    Log.append("Converting Java graph to SGraph with " + jGraph.getAllVertices.size + " nodes, " + jGraph.getAllEdges.size + " edges..")
    val timeConvertJavaToScalaGraph = CPUTimeDiff()

    val sGraph = new SGraph()
    jGraph.getAllVertices.map(v => sGraph.addVertex(convertJavaToScalaVertex(v)))
    jGraph.getAllEdges.map(e => sGraph.addEdge(convertJavaToScalaEdge(sGraph, e)))

    timeConvertJavaToScalaGraph.end
    Log.appendln(s" done in $timeConvertJavaToScalaGraph")

    sGraph
  }

  private def convertScalaToJavaVertex(sVertex: SVertex): graph.core.Vertex = {
    val jVertex = new graph.core.Vertex(sVertex.id)
    if(sVertex.containsObject)
      jVertex.setObjectId(sVertex.getObjectId)
    if(sVertex.getNodeLocation != null)
      jVertex.setNodeLocation(sVertex.getNodeLocation.x, sVertex.getNodeLocation.y)
    jVertex
  }

  private def convertJavaToScalaVertex(jVertex: graph.core.Vertex): SVertex = {
    val sVertex = new SVertex(jVertex.getId)
    if(jVertex.containsObject)
      sVertex.setObjectId(jVertex.getObjectId)
    if(jVertex.getNodeLocation != null)
      sVertex.setNodeLocation(jVertex.getNodeLocation.x, jVertex.getNodeLocation.y)
    sVertex
  }

  private def convertScalaToJavaEdge(jGraph: Graph, sEdge: SEdge): graph.core.Edge = {
    val jEdge = new graph.core.Edge(jGraph.getVertex(sEdge.source.id), jGraph.getVertex(sEdge.target.id))
    jEdge.setWeight(sEdge.getWeight)
    jEdge
  }

  private def convertJavaToScalaEdge(sGraph: SGraph, jEdge: graph.core.Edge): SEdge = {
    val sEdge = new SEdge(sGraph.getVertex(jEdge.getSource.getId), sGraph.getVertex(jEdge.getTarget.getId))
    sEdge.setWeight(jEdge.getWeight)
    sEdge
  }


  /**
   * @return A New example Graph from Figure 3 of paper TKDE - GraphRNN
   */
  def createExampleGraphEager: SGraph = {
    val n1 = new SVertex(1)
    val n2 = new SVertex(2)
    val n3 = new SVertex(3)
    val n4 = new SVertex(4)
    n4.setObjectId(0)       // query point
    val n5 = new SVertex(5)
    n5.setObjectId(2)
    val n6 = new SVertex(6)
    n6.setObjectId(1)
    val n7 = new SVertex(7)
    n7.setObjectId(3)

    val graph: SGraph = new SGraph()
    graph.addVertex(n1)
    graph.addVertex(n2)
    graph.addVertex(n3)
    graph.addVertex(n4)
    graph.addVertex(n5)
    graph.addVertex(n6)
    graph.addVertex(n7)

    val e41 = new SEdge(n4, n1)
    e41.setWeight(5)
    graph.addEdge(e41)

    val e17 = new SEdge(n1, n7)
    e17.setWeight(6)
    graph.addEdge(e17)

    val e72 = new SEdge(n7, n2)
    e72.setWeight(6)
    graph.addEdge(e72)

    val e26 = new SEdge(n2, n6)
    e26.setWeight(5)
    graph.addEdge(e26)

    val e63 = new SEdge(n6, n3)
    e63.setWeight(3)
    graph.addEdge(e63)

    val e34 = new SEdge(n3, n4)
    e34.setWeight(4)
    graph.addEdge(e34)

    val e15 = new SEdge(n1, n5)
    e15.setWeight(3)
    graph.addEdge(e15)

    val e53 = new SEdge(n5, n3)
    e53.setWeight(6)
    graph.addEdge(e53)

    val e52 = new SEdge(n5, n2)
    e52.setWeight(4)
    graph.addEdge(e52)

    graph
  }
}
