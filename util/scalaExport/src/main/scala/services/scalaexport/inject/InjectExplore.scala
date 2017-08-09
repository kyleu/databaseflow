package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

object InjectExplore {
  def injectMenu(result: ExportResult, rootDir: File) = {
    def queryFieldsFor(s: String) = {
      val newContent = result.tables.flatMap { et =>
        if (result.config.provided.contains(et.propertyName)) {
          None
        } else {
          val controllerClass = et.pkg match {
            case Nil => s"controllers.admin.routes.${et.className}Controller"
            case _ => s"controllers.admin.${et.pkg.mkString(".")}.routes.${et.className}Controller"
          }
          Some(s"""  <li><a href="@$controllerClass.list()">${et.title}</a></li>""")
        }

      }.sorted.mkString("\n")
      InjectHelper.replaceBetween(
        original = s, start = "  <!-- Start model list routes -->", end = "  <!-- End model list routes -->", newContent = newContent
      )
    }

    val schemaSourceFile = rootDir / "app" / "views" / "layout" / "adminMenu.scala.html"
    val newContent = queryFieldsFor(schemaSourceFile.contentAsString)
    schemaSourceFile.overwrite(newContent)

    "adminMenu.scala.html" -> newContent
  }

  def injectHtml(result: ExportResult, rootDir: File) = {
    def queryFieldsFor(s: String) = {
      val newContent = result.tables.flatMap { et =>
        if (result.config.provided.contains(et.propertyName)) {
          None
        } else {
          val controllerClass = et.pkg match {
            case Nil => s"controllers.admin.routes.${et.className}Controller"
            case _ => s"controllers.admin.${et.pkg.mkString(".")}.routes.${et.className}Controller"
          }
          Some(s"""      <li class="collection-item">
          <a class="theme-text" href="@$controllerClass.list()">${et.title} Management</a>
          <div><em>Manage the ${et.propertyName} of the system.</em></div>
        </li>""")
        }
      }.sorted.mkString("\n")

      InjectHelper.replaceBetween(
        original = s, start = "      <!-- Start model list routes -->", end = "      <!-- End model list routes -->", newContent = newContent
      )
    }

    val schemaSourceFile = rootDir / "app" / "views" / "admin" / "explore" / "explore.scala.html"
    val newContent = queryFieldsFor(schemaSourceFile.contentAsString)
    schemaSourceFile.overwrite(newContent)

    "explore.scala.html" -> newContent
  }
}
