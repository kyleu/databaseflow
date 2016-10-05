import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys
import com.typesafe.sbt.less.Import._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.jdkpackager.JDKPackagerPlugin
import com.typesafe.sbt.packager.linux.LinuxPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.windows.WindowsPlugin
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayImport.PlayKeys
import play.sbt.PlayImport.PlayKeys._
import play.sbt.routes.RoutesKeys.routesGenerator
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._
import sbtassembly.PathList
import webscalajs.WebScalaJS.autoImport._

object Server {
  private[this] val dependencies = {
    import Dependencies._
    Seq(
      Cache.ehCache, Akka.actor, Akka.logging, Play.playFilters, Play.playWs,
      Authentication.silhouette, Authentication.hasher, Authentication.persistence, Authentication.crypto,
      Export.csv, Export.xlsx, Ui.swing, Utils.crypto, Utils.scalaGuice, Utils.commonsIo,
      Akka.testkit, Play.playTest, Testing.scalaTest
    )
  }

  private[this] lazy val serverSettings = Shared.commonSettings ++ Seq(
    name := Shared.projectId,
    maintainer := "Kyle Unverferth <kyle@databaseflow.com>",
    description := "Database Flow",

    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= dependencies,

    scalaJSProjects := Seq(Client.client, Client.charting),
    routesGenerator := InjectedRoutesGenerator,
    externalizeResources := false,

    // Sbt-Web
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    pipelineStages := Seq(scalaJSPipeline, digest, gzip),
    includeFilter in (Assets, LessKeys.less) := "*.less",
    excludeFilter in (Assets, LessKeys.less) := "_*.less",
    LessKeys.compress in Assets := true,

    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value),

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

    // Code Quality
    scapegoatIgnoredFiles := Seq(".*/Routes.scala", ".*/ReverseRoutes.scala", ".*/JavaScriptReverseRoutes.scala", ".*/*.template.scala")
  )

  lazy val server = {
    val ret = Project(
      id = Shared.projectId,
      base = file(".")
    ).enablePlugins(
      SbtWeb, play.sbt.PlayScala, JavaAppPackaging,
      UniversalPlugin, LinuxPlugin, DebianPlugin, RpmPlugin, DockerPlugin, WindowsPlugin, JDKPackagerPlugin
    ).settings(serverSettings: _*).aggregate(projectToRef(Client.client)).settings(Packaging.settings: _*)

    Shared.withProjects(ret, Seq(Shared.sharedJvm, Database.dblibs, Utilities.metrics, Utilities.licenseModels))
  }
}
