import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys
import com.typesafe.sbt.less.Import._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys.routesGenerator
import webscalajs.WebScalaJS.autoImport._
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

object Site {
  private[this] val dependencies = {
    import Dependencies._
    Seq(
      Akka.actor, Akka.logging, Akka.testkit, Play.filters, Play.ws, Play.cache, Play.guice, Play.mailer, Play.mailerGuice, Play.test, Utils.betterFiles
    ) ++ Serialization.circeProjects.map(c => "io.circe" %% c % Dependencies.Serialization.circeVersion)
  }

  private[this] lazy val siteSettings = Shared.commonSettings ++ Seq(
    name := Shared.projectId + "-site",

    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= dependencies,

    libraryDependencies ++= Seq(Dependencies.Utils.enumeratum),

    routesGenerator := InjectedRoutesGenerator,
    PlayKeys.devSettings := Seq("play.server.akka.requestTimeout" -> "infinite"),

    // Sbt-Web
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,

    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),

    includeFilter in (Assets, LessKeys.less) := "*.less",
    excludeFilter in (Assets, LessKeys.less) := "_*.less",
    LessKeys.compress in Assets := true,

    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value),
    mainClass in assembly := Some("DatabaseFlow"),

    // Code Quality
    scapegoatIgnoredFiles := Seq(".*/Routes.scala", ".*/ReverseRoutes.scala", ".*/JavaScriptReverseRoutes.scala", ".*/*.template.scala")
  )

  lazy val site = {
    val ret = Project(id = "site", base = file("site")).enablePlugins(SbtWeb, play.sbt.PlayScala).settings(siteSettings: _*)
    Shared.withProjects(ret, Seq(Utilities.metrics))
  }
}
