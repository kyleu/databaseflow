import Dependencies._
import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import com.typesafe.sbt.SbtScalariform.{ ScalariformKeys, scalariformSettings }
import net.virtualvoid.sbt.graph.DependencyGraphSettings.graphSettings
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import playscalajs.ScalaJSPlay
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

object Shared {
  val projectId = "databaseflow"
  val projectName = "Database Flow"

  object Versions {
    val app = "1.0.0"
    val scala = "2.12.2"
  }

  val compileOptions = Seq(
    "target:jvm-1.8", "-encoding", "UTF-8", "-feature", "-deprecation", "-explaintypes", "-feature", "-unchecked",
    "–Xcheck-null", "-Xfatal-warnings", /* "-Xlint", */ "-Xcheckinit", "-Xfuture",
    "-Yno-adapted-args", "-Ywarn-dead-code", "-Ywarn-inaccessible", "-Ywarn-nullary-override", "-Ywarn-numeric-widen", "-Ywarn-infer-any"
  )

  lazy val commonSettings = Seq(
    version := Shared.Versions.app,
    scalaVersion := Shared.Versions.scala,

    scalacOptions ++= compileOptions,
    scalacOptions in Test ++= Seq("-Yrangepos"),

    // Packaging
    publishMavenStyle := false,

    test in assembly := {},
    assemblyMergeStrategy in assembly := {
      case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
      case PathList("javax", "xml", xs@_*) => MergeStrategy.first
      case PathList(p @ _*) if p.last.contains("about_jetty-") => MergeStrategy.discard
      case PathList("org", "apache", "commons", "logging", xs@_*) => MergeStrategy.first
      case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
      case PathList("sqlj", xs@_*) => MergeStrategy.first
      case "messages" => MergeStrategy.concat
      case "pom.xml" => MergeStrategy.discard
      case "JS_DEPENDENCIES" => MergeStrategy.discard
      case "pom.properties" => MergeStrategy.discard
      case "application.conf" => MergeStrategy.concat
      case x => (assemblyMergeStrategy in assembly).value(x)
    },

    // Prevent Scaladoc
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in packageDoc := false,
    sources in (Compile,doc) := Seq.empty,

    // Code Quality
    scapegoatVersion := Utils.scapegoatVersion,
    scapegoatDisabledInspections := Seq("MethodNames", "MethodReturningAny", "DuplicateImport"),
    scapegoatIgnoredFiles := Seq(".*/JsonSerializers.scala"),
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
  ) ++ graphSettings ++ scalariformSettings

  def withProjects(p: Project, includes: Seq[Project]) = includes.foldLeft(p) { (proj, inc) =>
    proj.aggregate(inc).dependsOn(inc)
  }

  lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).settings(commonSettings: _*).jsSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % Serialization.version,
      "com.beachape" %%% "enumeratum-upickle" % Utils.enumeratumVersion
    )
  ).jvmSettings(libraryDependencies ++= Seq(Serialization.uPickle, Utils.enumeratum))

  lazy val sharedJs = shared.js.enablePlugins(ScalaJSPlay).settings(scalaJSStage in Global := FastOptStage)

  lazy val sharedJvm = shared.jvm
}
