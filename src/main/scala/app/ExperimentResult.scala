package app

import Experiment.Experiment
import util.Utils.{writeToFile, formatThousands}

case class ExperimentResult(experiment                      : Experiment,
                            values                          : Seq[Any],
                            var algorithmResultsForEachValue: Seq[Seq[AlgorithmResult]]){


  /**
   * '####' will be replaced by a number of spaces as stored in "cellLengths"
   * @return
   */
  def toCSVString: String = {
    val sb = new StringBuilder()

    sb.append(
      s"""
       |---------------------------------------------------------------------------
       |${experiment.title} (${values mkString ", "})
       |---------------------------------------------------------------------------
       |
       |${ExperimentSetup.forExperiment(experiment, -1, null)}
       |
       |""".stripMargin)


    var cellLengths = IndexedSeq[Int]()
    // before  transpose: n x 3 (algorithms) lists
    // after   transpose: 3 (algorithms) x n lists
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
          val valuesString = (values mkString "#### ; ")
          if(!valuesString.isEmpty)
            valuesString + "####"
          else
            valuesString
          }
         |
         |${algorithmResults.head.singleResults.zipWithIndex
          .map(indexedSingleResult =>

            indexedSingleResult._1.name + "\n" +

            (algorithmResults.map(algorithmResult => {
              val runResults = algorithmResult.singleResults(indexedSingleResult._2).runResults.map(formatThousands(_)) mkString ", "
              cellLengths :+= runResults.size
              runResults + "####"
            }) mkString " ; ") + "\n" +

            (algorithmResults.map(algorithmResult => {
              val totalResult = formatThousands(algorithmResult.singleResults(indexedSingleResult._2).totalResult)
              cellLengths :+= totalResult.size
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

  def appendDirectComparison() = {
    val directComparisonString =
    s"""
      |${experiment.valueName};${values mkString ";"}
      |
      |""".stripMargin +
    (for{
      i <- 0 to Math.min(algorithmResultsForEachValue.transpose.head.head.singleResults.size - 1, 2)}  // From the first algorithm that runs (Naive), make comparisons for its single results between all algorithms
    yield {
      algorithmResultsForEachValue.transpose.head.head.singleResults(i).name + "\n" +
        (for{
          algorithmResults <- algorithmResultsForEachValue.transpose
        }
        yield{
          algorithmResults.head.algorithmName + ";" +
            (algorithmResults map { algorithmResult =>
              formatThousands(algorithmResult.singleResults(i).totalResult)
            }).mkString(";")
        }).mkString("\n")
    }).mkString("\n\n")

    writeToFile(s"log/${experiment.writeName}.txt", true, "\n\n\n\n" + directComparisonString)
  }

}