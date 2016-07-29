import Dependencies.{ Akka, Metrics, Play }
import com.typesafe.sbt.SbtScalariform.{ ScalariformKeys, scalariformSettings }
import net.virtualvoid.sbt.graph.DependencyGraphSettings.graphSettings
import sbt.Keys._
import sbt._
import io.gatling.sbt.GatlingPlugin
import pl.project13.scala.sbt.JmhPlugin

object Utilities {
  lazy val metrics = (project in file("util/metrics")).settings(
    name := "metrics",
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
  )
    .settings(libraryDependencies ++= Seq(
      Play.playLib, Akka.actor,
      Metrics.metrics, Metrics.healthChecks, Metrics.json, Metrics.jvm, Metrics.ehcache, Metrics.jettyServlet, Metrics.servlets, Metrics.graphite
    ))
    .settings(Shared.commonSettings: _*)
    .settings(graphSettings: _*)
    .settings(scalariformSettings: _*)

  lazy val iconCreator = (project in file("util/iconCreator")).settings(
    name := "icon-creator",
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
  )
    .settings(Shared.commonSettings: _*)
    .settings(graphSettings: _*)
    .settings(scalariformSettings: _*)

  lazy val licenseModels = (project in file("util/licenseModels")).settings(
    name := "license-models",
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
  )
    .settings(libraryDependencies ++= Seq(Dependencies.Utils.crypto, Dependencies.Utils.enumeratum))
    .settings(Shared.commonSettings: _*)
    .settings(graphSettings: _*)
    .settings(scalariformSettings: _*)

  lazy val licenseGenerator = (project in file("util/licenseGenerator")).settings(
    name := "license-generator",
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
  )
    .settings(Shared.commonSettings: _*)
    .settings(graphSettings: _*)
    .settings(scalariformSettings: _*)
    .dependsOn(licenseModels)
    .aggregate(licenseModels)

  lazy val benchmarking = (project in file("util/benchmarking")).settings(Shared.commonSettings: _*)
    .settings(
      name := "benchmarking",
      libraryDependencies ++= Seq(
        Dependencies.Testing.gatlingCore,
        Dependencies.Testing.gatlingCharts
      )
    )
    .enablePlugins(GatlingPlugin)
    .enablePlugins(JmhPlugin)
    .dependsOn(Shared.sharedJvm)
}
