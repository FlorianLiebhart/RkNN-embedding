package app

object Experiment extends Enumeration {

  type Experiment = Value

  implicit def valueToExperimentVal(x: Value) = x.asInstanceOf[Val]
  
  protected case class Val(nr: String, title: String, writeName: String, valueName: String) extends super.Val
  
  val Default = {
    val nr = "0"
    Val(
        nr              = nr,
        title = s"$nr. Experiment: Default run.",
        writeName       = s"$nr. Default",
        valueName       = "Default"
    )
  }

  val EntriesPerNode = {
    val nr = "1"
    Val(
        nr              = nr,
        title = s"$nr. Experiment: Alter number of entries per R*Tree node.",
        writeName       = s"$nr. Entries per node",
        valueName       = "Entries per node"
    )
  }

  val RefPoints = {
    val nr = "2"
    Val(
        nr              = nr,
        title = s"$nr. Experiment: Alter number of reference points.",
        writeName       = s"$nr. Reference",
        valueName       = "Reference points"
    )
  }

  val Vertices = {
    val nr = "3"
    Val(
        nr              = nr,
        title = s"$nr. Experiment: Alter number of vertices.",
        writeName       = s"$nr. Vertices",
        valueName       = "Vertices"
    )
  }

  val ObjectDensity = {
    val nr = "4"
    Val(
        nr              = nr,
        title = s"$nr. Experiment: Alter object density.",
        writeName       = s"$nr. Object density",
        valueName       = "Object density"
    )
  }

  val Connectivity = {
    val nr = "5"
    Val(
        nr              = nr,
        title = s"$nr. Experiment: Alter connectivity (number of edges).",
        writeName       = s"$nr. Connectivity",
        valueName       = "Connectivity"
    )
  }

  val K = {
    val nr = "6"
    Val(
        nr              = nr,
        title = s"$nr. Experiment: Alter k.",
        writeName       = s"$nr. k",
        valueName       = "k"
    )
  }
}