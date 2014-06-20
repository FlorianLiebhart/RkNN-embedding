package app

case class ExperimentResult(experimentTitle                 : String,
                            experimentValueName             : Experiment.Value,
                            values                          : Seq[Any],
                            var algorithmResultsForEachValue: Seq[Seq[AlgorithmResult]],
                            writeName                       : String){


  def toCSVString: String = {
    val sb = new StringBuilder()

    sb.append(
      s"""
       |---------------------------------------------------------------------------
       |${experimentTitle} (${values mkString ", "})
       |---------------------------------------------------------------------------
       |
       |${ExperimentSetup(experimentValueName = experimentValueName)}
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
         |${experimentValueName}:
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
    util.Log.write(s"log/$writeName.txt", false, toCSVString)
  }

}