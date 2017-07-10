import Dependencies._
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import com.typesafe.sbt.web.SbtWeb
import sbt.Keys._
import sbt._
import io.gatling.sbt.GatlingPlugin
import pl.project13.scala.sbt.JmhPlugin

object Utilities {
  private[this] val metricsLibs = Seq(
    Play.lib, Akka.actor,
    Metrics.metrics, Metrics.healthChecks, Metrics.json, Metrics.jvm, Metrics.ehcache, Metrics.jettyServlet, Metrics.servlets, Metrics.graphite
  )

  lazy val metrics = (project in file("util/metrics"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .settings(libraryDependencies ++= metricsLibs)
    .settings(Shared.commonSettings: _*)

  lazy val iconCreator = (project in file("util/iconCreator"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .settings(Shared.commonSettings: _*)

  lazy val scalaExport = (project in file("util/scalaExport"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .settings(Shared.commonSettings: _*)
    .settings(libraryDependencies ++= Seq(Utils.enumeratum, Utils.betterFiles, Utils.guava))
    .dependsOn(Shared.sharedJvm)

  lazy val translation = (project in file("util/translation"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .enablePlugins(SbtWeb, play.sbt.PlayScala)
    .settings(Shared.commonSettings: _*)
    .settings(libraryDependencies ++= Seq("com.beachape" %% "enumeratum-circe" % Utils.enumeratumCirceVersion, Play.ws))

  lazy val benchmarking = (project in file("util/benchmarking"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .settings(libraryDependencies ++= Seq(Testing.gatlingCore, Testing.gatlingCharts))
    .settings(Shared.commonSettings: _*)
    .enablePlugins(GatlingPlugin)
    .enablePlugins(JmhPlugin)
    .dependsOn(Shared.sharedJvm)
}
