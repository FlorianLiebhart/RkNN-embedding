package app

object Experiment extends Enumeration{
  type Experiment    = Value

  val default        = Value("Default run")

  val objectDensity  = Value("Object density")
  val vertices       = Value("Vertices")
  val connectivity   = Value("Connectivity")
  val k              = Value("k")
  val refPoints      = Value("Reference points")
  val entriesPerNode = Value("Entries per R*Tree node")
}
