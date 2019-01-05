import com.lightbend.paradox.sbt.ParadoxPlugin
import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport._
import com.typesafe.sbt.GitPlugin.autoImport.git
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import com.typesafe.sbt.site.SiteScaladocPlugin
import com.typesafe.sbt.site.SitePreviewPlugin.autoImport._
import com.typesafe.sbt.site.paradox.ParadoxSitePlugin
import com.typesafe.sbt.site.paradox.ParadoxSitePlugin.autoImport.Paradox
import sbt.{Project, file}
import sbt.Keys._
import sbt._
import _root_.io.github.jonas.paradox.material.theme.ParadoxMaterialThemePlugin

object Documentation {
  private[this] val SharedScaladocConfig = config("sharedScaladoc")
  private[this] val ClientScaladocConfig = config("clientScaladoc")
  private[this] val ServerScaladocConfig = config("serverScaladoc")

  lazy val doc = Project(id = "doc", base = file("./doc")).enablePlugins(
    ParadoxPlugin, ParadoxSitePlugin, SiteScaladocPlugin, GhpagesPlugin
  ).settings(Shared.commonSettings: _*).settings(ParadoxMaterialThemePlugin.paradoxMaterialThemeSettings(Paradox)).settings(
    git.remoteRepo := "git@github.com:KyleU/databaseflow.git",

    sourceDirectory in Paradox := sourceDirectory.value,
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    paradoxProperties ++= Map(
      "project.url" -> s"https://kyleu.github.io/${Shared.projectId}/",
      "github.base_url" -> s"https://github.com/KyleU/${Shared.projectId}/tree/${version.value}",
      "material.color.primary" -> "blue",
      "material.color.accent" -> "blue",
      "material.logo" -> "database",
      "material.favicon" -> "logo.png",
      "material.repo" -> "https://github.com/KyleU/databaseflow",
      "material.repo.type" -> "github",
      "material.repo.name" -> "KyleU/databaseflow",
      "material.author" -> "Kyle Unverferth",
      "material.custom.stylesheet" -> "databaseflow.css"
    ),

    previewLaunchBrowser := false,
    previewFixedPort := Some(4265),

    SiteScaladocPlugin.scaladocSettings(SharedScaladocConfig, mappings in (Compile, packageDoc) in Shared.sharedJvm, "api/shared"),
    SiteScaladocPlugin.scaladocSettings(ClientScaladocConfig, mappings in (Compile, packageDoc) in Client.client, "api/client"),
    SiteScaladocPlugin.scaladocSettings(ServerScaladocConfig, mappings in (Compile, packageDoc) in Server.server, "api/server")
  )
}
