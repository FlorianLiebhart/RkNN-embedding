package util

import Utils.writeToFile
import java.text.SimpleDateFormat
import java.util.Date
import app.RkNNTestEnvironment

object Log {
  val printEnabled  = false  // allow printing on console

  def print(s: Any):   Unit = if(printEnabled) System.out.print(s)
  def println(s: Any): Unit = if(printEnabled) System.out.println(s)


  val immedatePrint = false // print immedately after appending to log

  val printLog      = new StringBuilder()
  val writeLog      = new StringBuilder()

  val experimentLog = new StringBuilder()

  def experimentLogAppend(s: Any, inclTimeStamp: Boolean = true)   = {
    val timeStamp =
      if(inclTimeStamp)
        s"[${new SimpleDateFormat("HH:mm:ss").format(new Date())}] "
      else ""

    experimentLog.append(s"$timeStamp$s")
    writeLog.append(experimentLog)
    System.out.print(experimentLog.toString)
    writeToFile(s"log/${RkNNTestEnvironment.startDate}/experimentsLog.txt", true, experimentLog.toString)
    experimentLog.clear
  }

  def experimentLogAppendln(s: Any, inclTimeStamp: Boolean = true) = {
    val timeStamp =
      if(inclTimeStamp)
        s"[${new SimpleDateFormat("HH:mm:ss").format(new Date())}] "
      else ""

    experimentLog.append(s"$timeStamp$s\n")
    writeLog.append(experimentLog)
    System.out.print(experimentLog.toString)
    writeToFile(s"log/${RkNNTestEnvironment.startDate}/experimentsLog.txt", true, experimentLog.toString)
    experimentLog.clear
  }

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
    if (printEnabled){
      println(printLog.toString)
      printLog.clear
    }
  }

  def writeFlushWriteLog(path: String = s"log/${RkNNTestEnvironment.startDate}/writeLog.txt", appendToFile: Boolean){
    writeToFile(path, appendToFile, writeLog.toString)
    writeLog.clear
  }
}
