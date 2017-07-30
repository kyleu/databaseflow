package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult
import services.scalaexport.ExportHelper

object InjectIcons {
  def inject(result: ExportResult, rootDir: File) = {
    def iconFieldsFor(s: String) = {
      val newContent = result.models.map(m => s"""\n  val ${ExportHelper.toIdentifier(m._2)} = "fa-folder-o"""").sorted.mkString
      s.replaceAllLiterally("val user = \"fa-user\"", s"""val user = "fa-user"\n\n  //Model Icons$newContent""")
    }

    val iconSourceFile = rootDir / "shared" / "src" / "main" / "scala" / "models" / "template" / "Icons.scala"
    val newContent = iconFieldsFor(iconSourceFile.contentAsString)
    iconSourceFile.overwrite(newContent)

    "Icons.scala" -> newContent
  }
}
