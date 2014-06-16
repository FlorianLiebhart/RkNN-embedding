package graph

import scala.util.Random

import util.Utils.{TimeDiff, makesure}
import util.GuiConstants

/**
 * Created: 04.02.14, 16:42
 * @author fliebhart
 */
object GraphGen {

  def generateJavaGraph(numberOfVertices: Integer, numberOfEdges: Integer, numberOfObjects: Integer, weightOne: Boolean): graph.core.Graph = {
    checkPreconditions(numberOfVertices,numberOfEdges,numberOfObjects)

    println("Generating graph with " + numberOfVertices + " nodes, " + numberOfEdges + " edges, " + numberOfObjects + " objects..." )
    val timeGenerateGraph = TimeDiff()

    GraphGenerator.getInstance().generateProbabilisticGraph(numberOfVertices, numberOfEdges, 0, numberOfObjects, weightOne)

    timeGenerateGraph.end
    println(s"Graph generation finished. Runtime: $timeGenerateGraph")

    ProbabilisticGraph.getInstance()
  }

  def generateScalaGraph(numberOfVertices: Integer, numberOfEdges: Integer, numberOfObjects: Integer, edgeMaxWeight: Int): SGraph = {
    checkPreconditions(numberOfVertices, numberOfEdges, numberOfObjects)

    println("Generating graph with " + numberOfVertices + " nodes, " + numberOfEdges + " edges, " + numberOfObjects + " objects..." )
    val timeGenerateGraph = TimeDiff()


    val sGraph = new SGraph

    /*
     * create vertices
     */
    print(s"  - Creating $numberOfVertices vertices...")
    val timeCreateVertices = TimeDiff()

    (0 to numberOfVertices - 1) map { id => sGraph.addVertex(new SVertex(id)) }

    timeCreateVertices.end
    println(s" done in $timeCreateVertices")
    /*
     * create random objects
     */
    print(s"  - Creating $numberOfObjects random objects...")
    val timeCreateObjects = TimeDiff()

    val shuffledVertices = new Random(System.currentTimeMillis).shuffle(sGraph.getAllVertices)
    val objectIds        = (0 to numberOfObjects - 1)
    (shuffledVertices zip objectIds) map { case (v, id) => v.setObjectId(id) }

    timeCreateObjects.end
    println(s" done in $timeCreateObjects")

    /*
     * create node positions
     */
    print(s"  - Creating $numberOfVertices node positions...")
    val timeCreateNodePositions = TimeDiff()

    val nrOfRows = setNodesPosition(sGraph)

    timeCreateNodePositions.end
    println(s" done in $timeCreateNodePositions")

    /*
     * create random edges
     */
    print(s"  - Creating $numberOfEdges random edges...")
    val timeCreateEdges = TimeDiff()

    // create edges
    createRandomEdges(sGraph, numberOfEdges, nrOfRows)

    // create edge weights
    createRandomEdgeWeights(sGraph.getAllEdges, edgeMaxWeight)

    timeCreateEdges.end
    println(s" done in $timeCreateEdges")

    timeGenerateGraph.end
    println(s"Graph generation finished. Runtime: $timeGenerateGraph")

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
    makesure(numberOfObjects >= 0 && numberOfObjects <= numberOfVertices, s"Number of objects ($numberOfObjects) must be >= 0 and <= number of vertices ($numberOfVertices)")
    makesure(numberOfEdges >= numberOfVertices - 1,                       s"number of edges ($numberOfEdges) must be >= number of vertices (${numberOfVertices-1}) - 1.")
    val max: Integer = ((numberOfVertices.toDouble * (numberOfVertices.toDouble - 1)) / 2).toInt
    makesure(numberOfEdges <= max,                                        s"Number of edges ($numberOfEdges) must be <= $max for ${numberOfVertices} vertices")
  }
}
