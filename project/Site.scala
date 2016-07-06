import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys
import com.typesafe.sbt.jshint.Import.JshintKeys
import com.typesafe.sbt.less.Import._
import com.typesafe.sbt.rjs.Import._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.routes.RoutesKeys.routesGenerator
import playscalajs.PlayScalaJS.autoImport._
import sbt.Keys._
import sbt._

object Site {
  private[this] val dependencies = {
    import Dependencies._
    Seq(
      Cache.ehCache, Akka.actor, Akka.logging, Akka.testkit, Commerce.stripe,
      Play.playFilters, Play.playWs, Play.playTest, Play.playMailer,
      Metrics.metrics, Metrics.healthChecks, Metrics.json, Metrics.jvm, Metrics.ehcache, Metrics.jettyServlet, Metrics.servlets, Metrics.graphite,
      WebJars.requireJs, WebJars.jquery, WebJars.materialize, WebJars.fontAwesome
    )
  }

  private[this] lazy val siteSettings = Shared.commonSettings ++ Seq(
    name := Shared.projectId + "-site",

    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= dependencies,

    libraryDependencies += Dependencies.Utils.crypto,

    routesGenerator := InjectedRoutesGenerator,

    // Sbt-Web
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    pipelineStages := Seq(scalaJSProd, rjs, digest, gzip),
    includeFilter in (Assets, LessKeys.less) := "*.less",
    excludeFilter in (Assets, LessKeys.less) := "_*.less",
    LessKeys.compress in Assets := true,
    JshintKeys.config := Some(new java.io.File("conf/.jshintrc")),

    // Code Quality
    scapegoatIgnoredFiles := Seq(".*/Routes.scala", ".*/ReverseRoutes.scala", ".*/JavaScriptReverseRoutes.scala", ".*/*.template.scala")
  )

  lazy val site = Project(
    id = "site",
    base = file("site")
  )
    .enablePlugins(SbtWeb, play.sbt.PlayScala)
    .settings(siteSettings: _*)
    .dependsOn(Utilities.licenseGenerator)
    .aggregate(Utilities.licenseGenerator)
}
