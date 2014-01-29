import sbt._
import Keys._

object RkNNBuild extends Build {

  lazy val root = Project(id = "root", base = file(".")).settings(
    name := "RkNN",
    version := "1.0",
    scalaVersion := "2.10.3"
  )
}