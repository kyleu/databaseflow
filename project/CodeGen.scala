import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import com.typesafe.sbt.GitVersioning
import com.typesafe.sbt.SbtScalariform.{ScalariformKeys, defaultScalariformSettings}
import net.virtualvoid.sbt.graph.Plugin.graphSettings
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

object CodeGen {
  private[this] val dependencies = {
    import Dependencies._
    Seq(Utils.commonsIo, Utils.enumeratum, Hibernate.core)
  }

  private[this] lazy val codegenSettings = Seq(
    name := "Code Generator",
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

  lazy val codegen = Project(
    id = "codegen",
    base = file("codegen")
  )
    .enablePlugins(GitVersioning)
    .settings(codegenSettings: _*)
    .aggregate(Database.dblibs)
    .dependsOn(Database.dblibs)
}
