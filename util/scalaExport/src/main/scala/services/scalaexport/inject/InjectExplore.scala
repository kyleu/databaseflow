package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

object InjectExplore {
  def injectMenu(result: ExportResult, rootDir: File) = {
    def queryFieldsFor(s: String) = {
      val newContent = result.tables.map { et =>
        val controllerClass = et.pkg match {
          case Nil => s"controllers.admin.routes.${et.className}Controller"
          case _ => s"controllers.admin.${et.pkg.mkString(".")}.routes.${et.className}Controller"
        }
        s"""<li><a href="@$controllerClass.list()">${et.className}</a></li>"""
      }.sorted.mkString("\n  ")
      s.replaceAllLiterally("<!-- Other Models -->", newContent)
    }

    val schemaSourceFile = rootDir / "app" / "views" / "layout" / "adminMenu.scala.html"
    val newContent = queryFieldsFor(schemaSourceFile.contentAsString)
    schemaSourceFile.overwrite(newContent)

    "adminMenu.scala.html" -> newContent
  }

  def injectHtml(result: ExportResult, rootDir: File) = {
    def queryFieldsFor(s: String) = {
      val newContent = result.tables.map { et =>
        val controllerClass = et.pkg match {
          case Nil => s"controllers.admin.routes.${et.className}Controller"
          case _ => s"controllers.admin.${et.pkg.mkString(".")}.routes.${et.className}Controller"
        }
        s"""
      <li class="collection-item">
        <a class="theme-text" href="@$controllerClass.list()">${et.className} Management</a>
        <div><em>Manage the ${et.propertyName} of the system.</em></div>
      </li>
        """.trim()
      }.sorted.mkString("\n      ")
      s.replaceAllLiterally("<!-- Other Models -->", newContent)
    }

    val schemaSourceFile = rootDir / "app" / "views" / "admin" / "explore.scala.html"
    val newContent = queryFieldsFor(schemaSourceFile.contentAsString)
    schemaSourceFile.overwrite(newContent)

    "explore.scala.html" -> newContent
  }
}
