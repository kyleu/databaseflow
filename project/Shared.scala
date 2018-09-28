import Dependencies._
import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import com.typesafe.sbt.SbtScalariform.autoImport.scalariformAutoformat
import net.virtualvoid.sbt.graph.DependencyGraphSettings.graphSettings
import webscalajs.ScalaJSWeb
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType, _}
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._

object Shared {
  val projectId = "databaseflow"
  val projectName = "Database Flow"

  object Versions {
    val app = "1.6.0"
    val scala = "2.12.7"
  }

  val compileOptions = Seq(
    "target:jvm-1.8", "-encoding", "UTF-8", "-feature", "-deprecation", "-explaintypes", "-feature", "-unchecked",
    "–Xcheck-null", "-Xfatal-warnings", /* "-Xlint", */ "-Xcheckinit", "-Xfuture", "-Yrangepos", "-Ypartial-unification",
    "-Yno-adapted-args", "-Ywarn-dead-code", "-Ywarn-inaccessible", "-Ywarn-nullary-override", "-Ywarn-numeric-widen", "-Ywarn-infer-any"
  )

  lazy val commonSettings = Seq(
    organization := "com.databaseflow",

    licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.php")),
    homepage := Some(url("https://databaseflow.com")),
    scmInfo := Some(ScmInfo(url("https://github.com/KyleU/databaseflow"), "scm:git@github.com:KyleU/databaseflow.git")),
    developers := List(Developer(id = "kyleu", name = "Kyle Unverferth", email = "opensource@kyleu.com", url = url("http://kyleu.com"))),

    version := Shared.Versions.app,
    scalaVersion := Shared.Versions.scala,

    scalacOptions ++= compileOptions,
    scalacOptions in (Compile, console) ~= (_.filterNot(Set("-Ywarn-unused:imports", "-Xfatal-warnings"))),
    scalacOptions in (Compile, doc) := Seq("-encoding", "UTF-8"),

    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false).withWarnDirectEvictions(false),

    // Packaging
    publishMavenStyle := false,

    test in assembly := {},
    assemblyMergeStrategy in assembly := {
      case PathList("javax", "servlet", _ @ _*) => MergeStrategy.first
      case PathList("javax", "xml", _ @ _*) => MergeStrategy.first
      case PathList(p @ _*) if p.last.contains("about_jetty-") => MergeStrategy.discard
      case PathList("org", "apache", "commons", "logging", _ @ _*) => MergeStrategy.first
      case PathList("javax", "annotation", _ @ _*) => MergeStrategy.first
      case PathList("net", "jcip", "annotations", _ @ _*) => MergeStrategy.first
      case PathList("play", "api", "libs", "ws", _ @ _*) => MergeStrategy.first
      case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
      case PathList("sqlj", _ @ _*) => MergeStrategy.first
      case PathList("play", "reference-overrides.conf") => MergeStrategy.first
      case "module-info.class" => MergeStrategy.discard
      case "messages" => MergeStrategy.concat
      case "pom.xml" => MergeStrategy.discard
      case "JS_DEPENDENCIES" => MergeStrategy.discard
      case "pom.properties" => MergeStrategy.discard
      case "application.conf" => MergeStrategy.concat
      case x => (assemblyMergeStrategy in assembly).value(x)
    },

    // Publish Settings
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },

    publishTo := Some(xerial.sbt.Sonatype.autoImport.sonatypeDefaultResolver.value),
    // publishTo := Some("releases" at "http://nexus-1.fevo.com:8081/nexus/content/repositories/releases"),

    publishArtifact in Test := false,

    // Code Quality
    scapegoatVersion := Utils.scapegoatVersion,
    scapegoatDisabledInspections := Seq("MethodNames", "MethodReturningAny", "DuplicateImport"),
    scapegoatIgnoredFiles := Seq(".*/JsonSerializers.scala"),
    scalariformAutoformat := true
  ) ++ graphSettings

  def withProjects(p: Project, includes: Seq[Project]) = includes.foldLeft(p)((proj, inc) => proj.dependsOn(inc))

  lazy val shared = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("shared")).settings(commonSettings: _*).settings(
    name := "databaseflow-shared",
    libraryDependencies ++= Seq(
      "com.outr" %%% "scribe" % Utils.scribeVersion,
      "com.beachape" %%% "enumeratum-circe" % Dependencies.Utils.enumeratumCirceVersion
    ) ++ Serialization.circeProjects.map(c => "io.circe" %%% c % Dependencies.Serialization.circeVersion)
  )

  lazy val sharedJs = shared.js.enablePlugins(ScalaJSWeb)

  lazy val sharedJvm = shared.jvm
}
