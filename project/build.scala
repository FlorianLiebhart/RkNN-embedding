import sbt._
import Keys._

object RkNNBuild extends Build {

  val appName         = "RkNN"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.scalatest" %% "scalatest" % "1.9.2" % "test"
  )

  lazy val root = Project(id = "root", base = file(".")).settings(
    name := "hello",
    version := "1.0"      
  )

}
