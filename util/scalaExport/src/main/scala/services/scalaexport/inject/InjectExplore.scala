package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

object InjectExplore {
  private[this] def modelsFor(result: ExportResult) = {
    val filtered = result.models.filterNot(_.provided)
    val roots = filtered.filter(_.pkg.isEmpty).sortBy(_.title)
    val pkgGroups = filtered.filterNot(_.pkg.isEmpty).groupBy(_.pkg.head).mapValues(_.sortBy(_.title)).toSeq.sortBy(_._1)
    roots -> pkgGroups
  }

  def injectMenu(result: ExportResult, rootDir: File) = {
    def queryFieldsFor(s: String) = {
      val (roots, pkgGroups) = modelsFor(result)

      val newContent = (roots ++ pkgGroups.flatMap(_._2)).sortBy(_.title).map { model =>
        s"""  <li><a href="@${model.routesClass}.list()">${model.iconHtml} ${model.title}</a></li>"""
      }.mkString("\n")
      InjectHelper.replaceBetween(
        original = s, start = "  <!-- Start model list routes -->", end = "  <!-- End model list routes -->", newContent = newContent
      )
    }

    val schemaSourceFile = rootDir / "app" / "views" / "admin" / "layout" / "menu.scala.html"
    val newContent = queryFieldsFor(schemaSourceFile.contentAsString)
    schemaSourceFile.overwrite(newContent)

    "adminMenu.scala.html" -> newContent
  }

  def injectHtml(result: ExportResult, rootDir: File) = {
    def queryFieldsFor(s: String) = {
      val (roots, pkgGroups) = modelsFor(result)

      val newContent = (roots ++ pkgGroups.flatMap(_._2)).sortBy(_.title).map { model =>
        s"""    <li class="collection-item">
          |      <a class="theme-text" href="@${model.routesClass}.list()">${model.iconHtml} ${model.plural}</a>
          |      <div><em>Manage the ${model.plural} of the system.</em></div>
          |    </li>""".stripMargin
      }.mkString("\n")

      InjectHelper.replaceBetween(
        original = s, start = "    <!-- Start model list routes -->", end = "    <!-- End model list routes -->", newContent = newContent
      )
    }

    val schemaSourceFile = rootDir / "app" / "views" / "admin" / "explore" / "explore.scala.html"
    val newContent = queryFieldsFor(schemaSourceFile.contentAsString)
    schemaSourceFile.overwrite(newContent)

    "explore.scala.html" -> newContent
  }
}
