package services.scalaexport.db.file

import models.scalaexport.db.ExportEnum
import models.scalaexport.file.JsonFile

object EnumOpenApiPathsFile {
  def export(e: ExportEnum) = {
    val file = JsonFile("paths" +: e.pkg, e.propertyName)
    file.add("{", 1)
    file.add(s""""status": "TODO"""")
    file.add("}", -1)
    file
  }
}
