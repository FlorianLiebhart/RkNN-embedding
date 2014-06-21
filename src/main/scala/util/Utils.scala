package util

import graph.{SGraph, SVertex, SEdge}
import graph.core.{Graph, Vertex, Edge}
import scala.collection.JavaConversions._
import java.nio.file.{Files, Paths}
import java.io.{BufferedWriter, FileWriter}
import java.util.Date
import java.text.SimpleDateFormat
import java.lang.management.ManagementFactory


/**
 * Created: 28.01.14, 02:22
 * @author fliebhart
 */

object Log {

  val printEnabled  = true  // allow printing on console
  val immedatePrint = false // print immedately after appending to log

  def print(s: Any):   Unit = if(printEnabled) System.out.print(s)
  def println(s: Any): Unit = if(printEnabled) System.out.println(s)


  var nodesToVerify                          = 0
  var nodesVisited                           = 0
  var runTimeRknnQuery                       = 0

  var embeddingFilteredCandidates            = 0
  def setEmbeddingFilteredCandidates(x: Int) = { embeddingFilteredCandidates = x }
  var embeddingRunTimePreparation            = 0


  val printLog      = new StringBuilder()
  val writeLog      = new StringBuilder()

  val experimentLog = new StringBuilder()

  def experimentLogAppend(s: Any)   = experimentLog.append(s.toString)
  def experimentLogAppendln(s: Any) = experimentLog.append(s.toString + "\n")

  def append(s: Any) = {
    if (immedatePrint)
      System.out.print(s)
    else {
      printLog.append(s)
    }
    writeLog.append(s)
    this
  }

  def appendln(s: Any) = {
    if (immedatePrint)
      System.out.println(s)
    else {
      printLog.append(s + "\n")
    }
    writeLog.append(s + "\n")
    this
  }

  def printFlush = {
    println(printLog.toString)
    printLog.clear
  }

  def writeFlushWriteLog(appendToFile: Boolean){
    write(s"log/writeLog.txt", appendToFile, writeLog.toString)
    writeLog.clear
  }

  def write(destPath: String, appendToFile: Boolean, s: String) = {
    // create directories and file if non-existent
    val pathToFile = Paths.get(destPath)
    Files.createDirectories(pathToFile.getParent)
    if (!Files.exists(pathToFile))
        Files.createFile(pathToFile)
    val fw  = new FileWriter(destPath, appendToFile)
    val out = new BufferedWriter(fw)

    out.write(
      s
    )
    out.close()
  }

  def resetStats() {
    nodesToVerify               = 0
    nodesVisited                = 0
    runTimeRknnQuery            = 0

    embeddingFilteredCandidates = 0
    embeddingRunTimePreparation = 0
  }

}

object Utils {

  case class ThreadCPUTimeDiff(){
    val tStart: java.lang.Long = ManagementFactory.getThreadMXBean.getCurrentThreadCpuTime / 1000000

    private var tEnd: java.lang.Long = null

    def end   = { tEnd = ManagementFactory.getThreadMXBean.getCurrentThreadCpuTime / 1000000 }

    def diffMillis: Int = (tEnd - tStart).toInt

    override def toString: String = {
      if(tEnd == null)
        throw new RuntimeException("End time of TimeDiff not specified")

      else {
         s"{{{ $diffMillis ms. }}}"
       }
    }
  }

  /**
   * Throws IllegalArgumentException if b is false
   * @param b
   */
  def makesure(b: Boolean, errMsg: String) = if (!b) throw new IllegalArgumentException(errMsg)

  type VD = Tuple2[SVertex, Double] // (Vertex, Distance from q)
  implicit def t2ToOrdered(thisT2: VD): Ordered[VD] = new Ordered[VD] {
      def compare(otherT2: VD): Int = otherT2._2.compare(thisT2._2)
  }

  def convertScalaToJavaGraph(sGraph: SGraph): graph.core.Graph = {
    Log.append("\nConverting SGraph to Java graph with " + sGraph.getAllVertices.size + " nodes, " + sGraph.getAllEdges.size + " edges..")

    val timeConvertScalaToJavaGraph = ThreadCPUTimeDiff()

    val jGraph = new graph.core.Graph()
    sGraph.getAllVertices.map(v => jGraph.addVertex(convertScalaToJavaVertex(v)))
    sGraph.getAllEdges   .map(e => jGraph.addEdge  (convertScalaToJavaEdge(jGraph, e)))

    timeConvertScalaToJavaGraph.end
    Log.appendln(s" done in $timeConvertScalaToJavaGraph\n")

    jGraph
  }

  def convertJavaToScalaGraph(jGraph: graph.core.Graph): SGraph = {
    Log.append("Converting Java graph to SGraph with " + jGraph.getAllVertices.size + " nodes, " + jGraph.getAllEdges.size + " edges..")
    val timeConvertJavaToScalaGraph = ThreadCPUTimeDiff()

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
