package util

/**
 * Created: 21.06.14, 17:49
 * @author fliebhart
 */
object Stats {
  var nodesToVerify                          = 0
  var nodesVisited                           = 0
  var runTimeRknnQuery                       = 0

  var embeddingFilteredCandidates            = 0
  def setEmbeddingFilteredCandidates(x: Int) = { embeddingFilteredCandidates = x }
  var embeddingRunTimePreparation            = 0

  def reset() {
    nodesToVerify               = 0
    nodesVisited                = 0
    runTimeRknnQuery            = 0

    embeddingFilteredCandidates = 0
    embeddingRunTimePreparation = 0
  }
}
