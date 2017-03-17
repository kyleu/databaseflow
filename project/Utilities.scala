import Dependencies._
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import com.typesafe.sbt.web.SbtWeb
import sbt.Keys._
import sbt._
import io.gatling.sbt.GatlingPlugin
import pl.project13.scala.sbt.JmhPlugin

import scala.scalanative.sbtplugin.ScalaNativePlugin
import scala.scalanative.sbtplugin.ScalaNativePlugin.AutoImport._

object Utilities {
  private[this] val metricsLibs = Seq(
    Play.playLib, Akka.actor,
    Metrics.metrics, Metrics.healthChecks, Metrics.json, Metrics.jvm, Metrics.ehcache, Metrics.jettyServlet, Metrics.servlets, Metrics.graphite
  )

  lazy val metrics = (project in file("util/metrics"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .settings(libraryDependencies ++= metricsLibs)
    .settings(Shared.commonSettings: _*)

  lazy val iconCreator = (project in file("util/iconCreator"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .settings(Shared.commonSettings: _*)

  lazy val licenseModels = (project in file("util/licenseModels"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .settings(libraryDependencies ++= Seq(Utils.crypto, Utils.enumeratum))
    .settings(Shared.commonSettings: _*)

  lazy val licenseGenerator = (project in file("util/licenseGenerator"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .settings(Shared.commonSettings: _*)
    .dependsOn(licenseModels)
    .aggregate(licenseModels)

  lazy val translation = (project in file("util/translation"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .enablePlugins(SbtWeb, play.sbt.PlayScala)
    .settings(libraryDependencies ++= Seq(Utils.enumeratum, Play.playWs))
    .settings(Shared.commonSettings: _*)

  lazy val nativeSandbox = (project in file("util/nativeSandbox"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .enablePlugins(ScalaNativePlugin)
    .settings(nativeMode := "debug")
    //.settings(nativeMode := "release")
    .settings(Shared.commonSettings: _*)

  lazy val benchmarking = (project in file("util/benchmarking"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .settings(libraryDependencies ++= Seq(Testing.gatlingCore, Testing.gatlingCharts))
    .settings(Shared.commonSettings: _*)
    .enablePlugins(GatlingPlugin)
    .enablePlugins(JmhPlugin)
    .dependsOn(Shared.sharedJvm)
}
