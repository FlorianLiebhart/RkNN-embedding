package util

import Utils.writeToFile

object Log {
  val printEnabled  = true  // allow printing on console

  def print(s: Any):   Unit = if(printEnabled) System.out.print(s)
  def println(s: Any): Unit = if(printEnabled) System.out.println(s)


  val immedatePrint = false // print immedately after appending to log

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
    writeToFile(s"log/writeLog.txt", appendToFile, writeLog.toString)
    writeLog.clear
  }
}
