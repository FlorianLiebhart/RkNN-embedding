package app

import Experiment.Experiment
import util.Utils.writeToFile

case class ExperimentResult(experiment                      : Experiment,
                            values                          : Seq[Any],
                            var algorithmResultsForEachValue: Seq[Seq[AlgorithmResult]]){


  def toCSVString: String = {
    val sb = new StringBuilder()

    sb.append(
      s"""
       |---------------------------------------------------------------------------
       |${experiment.title} (${values mkString ", "})
       |---------------------------------------------------------------------------
       |
       |${ExperimentSetup(experiment)}
       |
       |""".stripMargin)


    var cellLengths = IndexedSeq[Int]()
    // vor  transpose: n listen x 3
    // nach transpose: 3 listen x n
    for (algorithmResults <- algorithmResultsForEachValue.transpose) {
      sb.append(
        s"""
         |${algorithmResults.head.algorithmName} (${algorithmResults.head.runs} runs)
         |-----------------
         |
         |${experiment.valueName}:
         |${
          for (v <- values)
            cellLengths :+= v.toString.size
          (values mkString "#### ; ") + "####"
          }
         |
         |${algorithmResults.head.singleResults.zipWithIndex
          .map(indexedSingleResult =>

            indexedSingleResult._1.name + "\n" +

            (algorithmResults.map(algorithmResult => {
              val runResults = algorithmResult.singleResults(indexedSingleResult._2).runResults mkString ", "
              cellLengths :+= runResults.size
              runResults + "####"
            }) mkString " ; ") + "\n" +

            (algorithmResults.map(algorithmResult => {
              val totalResult = algorithmResult.singleResults(indexedSingleResult._2).totalResult
              cellLengths :+= totalResult.toString.size
              totalResult + "####"
            }) mkString " ; ")

          ) mkString "\n\n"
          }
         |
         |""".stripMargin
      )
    }
    (sb.toString.split("####") zip cellLengths) map { x =>
      x._1 + (Seq.fill(cellLengths.max - x._2)(" ") mkString)
    } mkString
  }

  def write() = {
    writeToFile(s"log/${experiment.writeName}.txt", false, toCSVString)
  }

}