package services.scalaexport.db.file

import models.scalaexport.db.ExportEnum
import models.scalaexport.file.ScalaFile

object EnumSchemaFile {
  def export(enum: ExportEnum) = {
    val file = ScalaFile(enum.modelPackage, enum.className + "Schema", None)

    file.addImport("models.graphql", "CommonSchema")
    file.addImport("sangria.schema", "EnumType")

    file.add(s"object ${enum.className}Schema {", 1)
    file.add(s"implicit val ${enum.propertyName}EnumType: EnumType[${enum.className}] = CommonSchema.deriveStringEnumeratumType(", 1)
    file.add(s"""name = "${enum.className}",""")
    file.add(s"""description = "An enumeration of ${enum.className} values.",""")
    file.add(s"values = ${enum.className}.values.map(t => t -> t.value).toList")
    file.add(")", -1)
    file.add("}", -1)

    file
  }
}
