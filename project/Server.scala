import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys
import com.typesafe.sbt.jshint.Import.JshintKeys
import com.typesafe.sbt.less.Import._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.jdkpackager.JDKPackagerPlugin.autoImport._
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.jdkpackager.JDKPackagerPlugin
import com.typesafe.sbt.packager.linux.LinuxPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.windows.WindowsPlugin
import com.typesafe.sbt.rjs.Import._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.routes.RoutesKeys.routesGenerator
import playscalajs.PlayScalaJS.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

object Server {
  private[this] val dependencies = {
    import Dependencies._
    Seq(
      Cache.ehCache, Mail.mailer, Akka.actor, Akka.logging, Akka.testkit, Authentication.silhouette, Play.playFilters, Play.playWs, Play.playTest,
      Metrics.metrics, Metrics.healthChecks, Metrics.json, Metrics.jvm, Metrics.ehcache, Metrics.jettyServlet, Metrics.servlets, Metrics.graphite,
      WebJars.requireJs, WebJars.jquery, WebJars.materialize, WebJars.fontAwesome
    )
  }

  private[this] lazy val serverSettings = Shared.commonSettings ++ Seq(
    name := Shared.projectName,
    maintainer := "Kyle Unverferth",
    description := "Database Flow is pretty great.",

    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= dependencies,

    scalaJSProjects := Seq(Client.client),

    routesGenerator := InjectedRoutesGenerator,

    // Sbt-Web
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    pipelineStages := Seq(scalaJSProd, rjs, digest, gzip),
    includeFilter in (Assets, LessKeys.less) := "*.less",
    excludeFilter in (Assets, LessKeys.less) := "_*.less",
    LessKeys.compress in Assets := true,
    JshintKeys.config := Some(new java.io.File("conf/.jshintrc")),

    // Native Packaging
    topLevelDirectory := Some("DatabaseFlow"),
    packageSummary := description.value,
    packageDescription := "Database Flow helps you do all sorts of cool stuff.",
    rpmVendor := "Database Flow",
    wixProductId := "5fee44ae-0989-429b-9b1a-de8ec7dd9af5",
    wixProductUpgradeId := "6d353c6a-6f39-48f1-afa8-2c5eb726a8b8",
    //sourceDirectories in JDKPackager += "",
    jdkAppIcon := (sourceDirectory.value ** iconGlob).getPaths.headOption.map(file),
    jdkPackagerType := "installer",
    jdkPackagerJVMArgs := Seq("-Xmx2g"),
    jdkPackagerToolkit := SwingToolkit,
    jdkPackagerProperties := Map("app.name" -> name.value, "app.version" -> version.value),

    // Code Quality
    scapegoatIgnoredFiles := Seq(".*/Row.scala", ".*/Routes.scala", ".*/ReverseRoutes.scala", ".*/JavaScriptReverseRoutes.scala", ".*/*.template.scala"),
    scapegoatDisabledInspections := Seq("DuplicateImport")
  )

  lazy val iconGlob = sys.props("os.name").toLowerCase match {
    case os if os.contains("mac") ⇒ "*.icns"
    case os if os.contains("win") ⇒ "*.ico"
    case _ ⇒ "*.png"
  }

  lazy val server = Project(
    id = Shared.projectId,
    base = file(".")
  )
    .enablePlugins(SbtWeb, play.sbt.PlayScala)
    .enablePlugins(UniversalPlugin, LinuxPlugin, DebianPlugin, RpmPlugin, DockerPlugin, WindowsPlugin, JDKPackagerPlugin)
    .settings(serverSettings: _*)
    .aggregate(projectToRef(Client.client))
    .aggregate(Shared.sharedJvm)
    .aggregate(Database.dblibs)
    .dependsOn(Database.dblibs)
}
