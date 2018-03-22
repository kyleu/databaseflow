package services.scalaexport.db.file

import models.scalaexport.db.ExportEnum
import models.scalaexport.file.GraphQLQueryFile

object EnumGraphQLQueryFile {
  def export(enum: ExportEnum) = {
    val file = GraphQLQueryFile(enum.pkg, enum.className)

    file.add(s"# Retrieves the list of possible ${enum.className} values.")
    file.add(s"query ${enum.className} {", 1)
    file.add(enum.propertyName)
    file.add("}", -1)

    file
  }
}
