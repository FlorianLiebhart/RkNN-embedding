package app

import app.Experiment.Experiment

case class AlgorithmResult(algorithmName   : String,
                           runs            : Int,
                           nrOfQueryPoints : Int,
                           experiment      : Experiment,
                           singleResults   : Seq[SingleResult]) {
}
