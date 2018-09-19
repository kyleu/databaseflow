import com.lightbend.paradox.sbt.ParadoxPlugin
import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport._
import com.typesafe.sbt.GitPlugin.autoImport.git
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import com.typesafe.sbt.site.SiteScaladocPlugin
import com.typesafe.sbt.site.SitePreviewPlugin.autoImport._
import com.typesafe.sbt.site.paradox.ParadoxSitePlugin
import com.typesafe.sbt.site.paradox.ParadoxSitePlugin.autoImport.Paradox
import sbt.{ Project, file }
import sbt.Keys._
import sbt._

object Documentation {
  private[this] val SharedScaladocConfig = config("sharedScaladoc")
  private[this] val ClientScaladocConfig = config("clientScaladoc")
  private[this] val EbenezerScaladocConfig = config("ebenezerScaladoc")
  private[this] val ScalaExportScaladocConfig = config("scalaExportScaladoc")
  private[this] val ServerScaladocConfig = config("serverScaladoc")

  lazy val documentedDependencies = {
    import Dependencies._
    def toKV(d: ModuleID) = s"${d.name}.version" -> d.revision
    val ret = Seq(Play.lib, Akka.actor, GraphQL.sangria, Jdbc.hikariCp, Authentication.silhouette, Metrics.metrics).map(toKV).toMap
    println(ret)
    ret
  }

  lazy val doc = Project(id = "doc", base = file("./doc")).enablePlugins(
    ParadoxPlugin, ParadoxSitePlugin, SiteScaladocPlugin, GhpagesPlugin
  ).settings(Shared.commonSettings: _*).settings(
    git.remoteRepo := "git@github.com:KyleU/databaseflow.git",

    sourceDirectory in Paradox := sourceDirectory.value,
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    paradoxProperties ++= Map(
      "project.url" -> s"https://kyleu.github.io/${Shared.projectId}/",
      "github.base_url" -> s"https://github.com/KyleU/${Shared.projectId}/tree/${version.value}",
      "scaladoc.base_url" -> s"https://kyleu.github.io/api/server",
      "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/akka/${Dependencies.Akka.version}",
      "extref.rfc.base_url" -> "http://tools.ietf.org/html/rfc%s",
      "scalajs.version" -> Dependencies.ScalaJS.version,
      "circe.version" -> Dependencies.Serialization.circeVersion,
      "enumeratum.version" -> Dependencies.Utils.enumeratumVersion,
      "scalatags.version" -> Dependencies.ScalaJS.scalaTagsVersion
    ) ++ documentedDependencies,

    previewLaunchBrowser := false,
    previewFixedPort := Some(4265),

    SiteScaladocPlugin.scaladocSettings(SharedScaladocConfig, mappings in (Compile, packageDoc) in Shared.sharedJvm, "api/shared"),
    SiteScaladocPlugin.scaladocSettings(ClientScaladocConfig, mappings in (Compile, packageDoc) in Client.client, "api/client"),
    SiteScaladocPlugin.scaladocSettings(ScalaExportScaladocConfig, mappings in (Compile, packageDoc) in Utilities.scalaExport, "api/scalaExport"),
    SiteScaladocPlugin.scaladocSettings(EbenezerScaladocConfig, mappings in (Compile, packageDoc) in Utilities.ebenezer, "api/ebenezer"),
    SiteScaladocPlugin.scaladocSettings(ServerScaladocConfig, mappings in (Compile, packageDoc) in Server.server, "api/server")
  )
}
