package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportEnum
import com.databaseflow.models.scalaexport.file.JsonFile

object EnumOpenApiSchemaFile {
  def export(e: ExportEnum) = {
    val file = JsonFile("components" +: "schema" +: e.pkg, e.propertyName)
    file.add("{", 1)
    file.add(s""""${e.fullClassName}": {""", 1)

    file.add("}", -1)
    file.add("}", -1)
    file
  }
}
