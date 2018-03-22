package services.scalaexport.db.file

import models.scalaexport.db.ExportEnum
import models.scalaexport.file.RestQueryFile

object EnumRestQueryFile {
  def export(enum: ExportEnum) = {
    val file = RestQueryFile(enum.pkg, enum.className)

    file.add("{", 1)
    file.add(s""""title": "${enum.propertyName}",""")
    file.add(s""""description": "Retrieves the list of possible ${enum.className} values. [Generated File]"""")
    file.add("}", -1)

    file
  }
}
