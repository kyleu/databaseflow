import Dependencies._
import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import com.typesafe.sbt.SbtScalariform.{ ScalariformKeys, scalariformSettings }
import net.virtualvoid.sbt.graph.Plugin.graphSettings
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import playscalajs.ScalaJSPlay
import playscalajs.ScalaJSPlay.autoImport._
import sbt.Keys._
import sbt._

object Shared {
  val projectId = "databaseflow"
  val projectName = "Database Flow"

  lazy val commonSettings = Seq(
    version := Shared.Versions.app,
    scalaVersion := Shared.Versions.scala,

    scalacOptions ++= Seq(
      "-encoding", "UTF-8", "-feature", "-deprecation:false", "-unchecked", "–Xcheck-null", "-Xfatal-warnings", "-Xlint",
      "-Ywarn-adapted-args", "-Ywarn-dead-code", "-Ywarn-inaccessible", "-Ywarn-nullary-override", "-Ywarn-numeric-widen"
    ),
    scalacOptions in Test ++= Seq("-Yrangepos"),

    publishMavenStyle := false,

    // Prevent Scaladoc
    doc in Compile <<= target.map(_ / "none"),
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,

    // Code Quality
    scapegoatVersion := Utils.scapegoatVersion,
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
  ) ++ graphSettings ++ scalariformSettings


  object Versions {
    val app = "1.0.0"
    val scala = "2.11.8"
  }

  lazy val sharedJs = (crossProject.crossType(CrossType.Pure) in file("shared")).settings(commonSettings: _*).settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % Serialization.version,
      "com.lihaoyi" %%% "scalatags" % Templating.version,
      "com.beachape" %%% "enumeratum-upickle" % Utils.enumeratumVersion
    )
  )
    .enablePlugins(ScalaJSPlay)
    .settings(
      scalaJSStage in Global := FastOptStage
    ).js

  lazy val sharedJvm = (project in file("shared")).settings(commonSettings: _*).settings(
    libraryDependencies ++= Seq(
      Serialization.uPickle,
      Templating.scalaTags,
      Utils.enumeratum
    )
  )
}
