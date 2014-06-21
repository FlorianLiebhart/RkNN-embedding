package util

import java.nio.file.{Files, Paths}
import java.io.{BufferedWriter, FileWriter}

import graph.SVertex

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

  def writeToFile(destPath: String, appendToFile: Boolean, s: String) = {
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
