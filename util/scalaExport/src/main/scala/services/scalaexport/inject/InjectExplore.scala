package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

object InjectExplore {
  def inject(result: ExportResult, rootDir: File) = {
    def queryFieldsFor(s: String) = {
      val newContent = result.models.map { m =>
        s"""  <li><a href="">${m._2}</a></li>"""
      }.sorted.mkString("\n").stripPrefix("  ")
      s.replaceAllLiterally("<!-- Other Models -->", newContent)
    }

    val schemaSourceFile = rootDir / "app" / "views" / "layout" / "adminMenu.scala.html"
    val newContent = queryFieldsFor(schemaSourceFile.contentAsString)
    schemaSourceFile.overwrite(newContent)

    "Schema.scala" -> newContent
  }
}
