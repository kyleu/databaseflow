import Dependencies._
import com.typesafe.sbt.web.SbtWeb
import sbt.Keys._
import sbt._
// import io.gatling.sbt.GatlingPlugin
// import pl.project13.scala.sbt.JmhPlugin

object Utilities {
  lazy val benchmarking = (project in file("util/benchmarking")).settings(Shared.commonSettings: _*).settings(
    libraryDependencies ++= Seq(Testing.gatlingCore, Testing.gatlingCharts)
  )//.enablePlugins(GatlingPlugin).enablePlugins(JmhPlugin).dependsOn(Shared.sharedJvm)

  lazy val metrics = (project in file("util/metrics")).settings(libraryDependencies ++= Seq(
    Play.lib, Akka.actor,
    Metrics.metrics, Metrics.healthChecks, Metrics.json, Metrics.jvm, Metrics.ehcache, Metrics.jettyServlet, Metrics.servlets, Metrics.graphite
  )).settings(Shared.commonSettings: _*)

  lazy val translation = (project in file("util/translation")).enablePlugins(SbtWeb, play.sbt.PlayScala).settings(Shared.commonSettings: _*).settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % Dependencies.Serialization.circeVersion,
      "io.circe" %% "circe-generic" % Dependencies.Serialization.circeVersion,
      "io.circe" %% "circe-generic-extras" % Dependencies.Serialization.circeVersion,
      "io.circe" %% "circe-parser" % Dependencies.Serialization.circeVersion,
      "com.beachape" %% "enumeratum-circe" % Utils.enumeratumCirceVersion,
      Play.ws,
      Play.guice
    )
  )
}
