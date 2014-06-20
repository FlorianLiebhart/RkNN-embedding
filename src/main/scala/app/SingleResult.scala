package app

/**
 *
 * @param name e.g. "Candidates to refine on graph"
 * @param runResults  e.g. "123,234,345,456"
 * @param totalResults e.g. "333"
 */
case class SingleResult(name: String, runResults: Seq[Int]) {
  val totalResult = runResults.fold(0)(_ + _) / runResults.size
}