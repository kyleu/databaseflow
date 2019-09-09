import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys
import com.typesafe.sbt.less.Import._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.jdkpackager.JDKPackagerPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys.routesGenerator
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._
import webscalajs.WebScalaJS.autoImport._

object Server {
  private[this] val dependencies = {
    import Dependencies._
    Seq(
      Play.filters, Play.guice, Play.ws, Play.cache, GraphQL.sangria, GraphQL.playJson, GraphQL.circe,
      Authentication.silhouette, Authentication.hasher, Authentication.persistence, Authentication.crypto,
      Export.csv, Utils.scalaGuice, Utils.commonsIo, Utils.commonsLang, Utils.fastparse, Utils.betterFiles,
      Testing.scalaTest
    )
  }

  private[this] lazy val serverSettings = Shared.commonSettings ++ Seq(
    name := Shared.projectId,
    maintainer := "Kyle Unverferth <kyle@databaseflow.com>",
    description := "Database Flow",

    resolvers += "Atlassian Maven Repository" at "https://maven.atlassian.com/repository/public",

    libraryDependencies ++= dependencies,

    scalaJSProjects := Seq(Client.client, Client.charting),
    routesGenerator := InjectedRoutesGenerator,
    PlayKeys.externalizeResources := false,
    PlayKeys.devSettings := Seq("play.server.akka.requestTimeout" -> "infinite"),
    PlayKeys.playInteractionMode := PlayUtils.NonBlockingInteractionMode,

    // Sbt-Web
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,

    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(gzip),

    includeFilter in (Assets, LessKeys.less) := "*.less",
    excludeFilter in (Assets, LessKeys.less) := "_*.less",
    LessKeys.compress in Assets := true,

    assemblyJarName in assembly := s"${Shared.projectId}.jar",
    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value),
    mainClass in assembly := Some("DatabaseFlow")
  )

  lazy val server = {
    val ret = Project(
      id = Shared.projectId,
      base = file(".")
    ).enablePlugins(
      SbtWeb, play.sbt.PlayScala, JavaAppPackaging, UniversalPlugin, DockerPlugin, JDKPackagerPlugin
    ).settings(serverSettings: _*).settings(Packaging.settings: _*)

    Shared.withProjects(ret, Seq(Shared.sharedJvm, Database.dblibs))
  }
}
