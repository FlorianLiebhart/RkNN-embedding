package app

case class AlgorithmResult(algorithmName: String,
                           runs         : Int,
                           valueName    : Experiment.Value,
                           value        : Any,
                           singleResults: Seq[SingleResult]) {
}
