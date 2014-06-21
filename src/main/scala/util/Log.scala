package util

import java.nio.file.{Files, Paths}
import java.io.{BufferedWriter, FileWriter}

object Log {
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
