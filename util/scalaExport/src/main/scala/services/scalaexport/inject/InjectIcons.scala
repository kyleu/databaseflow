package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

object InjectIcons {
  def inject(result: ExportResult, rootDir: File) = {
    def iconFieldsFor(s: String) = {
      val startString = "  // Start model icons"
      val startIndex = s.indexOf(startString)
      val newContent = result.models.flatMap { m =>
        s.indexOf(m.propertyName) match {
          case x if x > -1 && x < startIndex => None
          case _ => Some(s"""  val ${m.propertyName} = "fa-folder-o"""")
        }
      }.sorted.mkString("\n")
      InjectHelper.replaceBetween(original = s, start = startString, end = "  // End model icons", newContent = newContent)
    }

    val iconSourceFile = rootDir / "shared" / "src" / "main" / "scala" / "models" / "template" / "Icons.scala"
    val newContent = iconFieldsFor(iconSourceFile.contentAsString)
    iconSourceFile.overwrite(newContent)

    "Icons.scala" -> newContent
  }
}
