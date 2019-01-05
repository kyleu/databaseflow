import com.lightbend.paradox.sbt.ParadoxPlugin
import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport._
import com.typesafe.sbt.GitPlugin.autoImport.git
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import com.typesafe.sbt.site.SiteScaladocPlugin
import com.typesafe.sbt.site.paradox.ParadoxSitePlugin
import com.typesafe.sbt.site.paradox.ParadoxSitePlugin.autoImport.Paradox
import sbt.{Project, file}
import sbt.Keys._
import sbt._
import _root_.io.github.jonas.paradox.material.theme.ParadoxMaterialThemePlugin
import com.typesafe.sbt.site.SitePreviewPlugin.autoImport.previewLaunchBrowser

object Documentation {
  lazy val doc = Project(id = "doc", base = file("./doc")).settings(Shared.commonSettings: _*).enablePlugins(
    ParadoxPlugin, ParadoxSitePlugin, SiteScaladocPlugin, GhpagesPlugin, ParadoxMaterialThemePlugin
  ).settings(ParadoxMaterialThemePlugin.paradoxMaterialThemeSettings(Paradox)).settings(
    sourceDirectory in Paradox := sourceDirectory.value,
    git.remoteRepo := "git@github.com:KyleU/databaseflow.git",

    paradoxProperties ++= Map(
      "material.logo" -> "database",
      "material.favicon" -> "logo.png",
      "material.repo" -> "https://github.com/KyleU/databaseflow",
      "material.repo.type" -> "github",
      "material.repo.name" -> "KyleU/databaseflow",
      "material.author" -> "Kyle Unverferth",
      "material.custom.stylesheet" -> "databaseflow.css"
    ),

    previewLaunchBrowser := false
  )
}
