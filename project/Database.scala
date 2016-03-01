import sbt._
import sbt.Keys._
import sbt.Project.projectToRef

import com.typesafe.sbt.GitVersioning

import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import com.typesafe.sbt.SbtScalariform.{ ScalariformKeys, defaultScalariformSettings }
import net.virtualvoid.sbt.graph.Plugin.graphSettings

object Database {
  private[this] val dependencies = {
    import Dependencies._
    Seq(
      Logging.slf4jApi, Jdbc.hikariCp,
      Jdbc.h2, Jdbc.mysql, Jdbc.postgres
    )
  }

  private[this] lazy val dblibsSettings = Seq(
    name := "Database Library",
    version := Shared.Versions.app,
    scalaVersion := Shared.Versions.scala,

    scalacOptions ++= Shared.compileOptions,
    scalacOptions in Test ++= Seq("-Yrangepos"),

    libraryDependencies ++= dependencies,

    // Code Quality
    scapegoatVersion := Dependencies.scapegoatVersion,
    ScalariformKeys.preferences := ScalariformKeys.preferences.value,

    publishMavenStyle := false
  ) ++ graphSettings ++ defaultScalariformSettings

  lazy val dblibs = Project(
    id = "dblibs",
    base = file("dblibs")
  )
    .enablePlugins(GitVersioning)
    .settings(dblibsSettings: _*)
    .aggregate(Shared.sharedJvm)
    .dependsOn(Shared.sharedJvm)
}
