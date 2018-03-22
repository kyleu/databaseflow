package services.scalaexport.db.file

import models.scalaexport.db.ExportModel
import models.scalaexport.file.RestQueryFile

object ModelRestQueryFile {
  def export(model: ExportModel) = {
    val file = RestQueryFile(model.pkg, model.className)
    file.add("{", 1)
    file.add(s""""title": "${model.propertyName}",""")
    file.add(s""""description": "Queries the system for ${model.plural}. [Generated File]"""")
    file.add("}", -1)
    file
  }
}
