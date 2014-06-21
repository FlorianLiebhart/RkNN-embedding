package util

import graph.SVertex
import java.nio.file.{Files, Paths}
import java.io.{BufferedWriter, FileWriter}

object Log {
  var nodesToVerify                          = 0
  var nodesVisited                           = 0
  var runTimeRknnQuery                       = 0

  var embeddingFilteredCandidates            = 0
  def setEmbeddingFilteredCandidates(x: Int) = { embeddingFilteredCandidates = x }
  var embeddingRunTimePreparation            = 0

  def resetStats() {
    nodesToVerify               = 0
    nodesVisited                = 0
    runTimeRknnQuery            = 0

    embeddingFilteredCandidates = 0
    embeddingRunTimePreparation = 0
  }


  val printEnabled  = true  // allow printing on console
  val immedatePrint = false // print immedately after appending to log

  def print(s: Any):   Unit = if(printEnabled) System.out.print(s)
  def println(s: Any): Unit = if(printEnabled) System.out.println(s)

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
}

object Utils {

  /**
   * Throws IllegalArgumentException if b is false
   * @param b
   */
  def makesure(b: Boolean, errMsg: String) = if (!b) throw new IllegalArgumentException(errMsg)

  type VD = Tuple2[SVertex, Double] // (Vertex, Distance from q)
  implicit def t2ToOrdered(thisT2: VD): Ordered[VD] = new Ordered[VD] {
      def compare(otherT2: VD): Int = otherT2._2.compare(thisT2._2)
  }
}
