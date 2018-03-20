package services.scalaexport.db.file

import models.scalaexport.db.ExportEnum
import models.scalaexport.file.GraphQLQueryFile

object EnumGraphQLQueryFile {
  def export(enum: ExportEnum) = {
    val key = enum.className + "GetAll"
    val file = GraphQLQueryFile(enum.modelPackage, key)

    file.add(s"query $key {", 1)
    file.add(enum.propertyName)
    file.add("}", -1)

    file
  }
}
