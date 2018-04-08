package services.scalaexport.db.file

import models.scalaexport.db.ExportEnum
import models.scalaexport.file.ScalaFile

object EnumSchemaFile {
  def export(enum: ExportEnum) = {
    val file = ScalaFile(enum.modelPackage, enum.className + "Schema", None)

    file.addImport("models.graphql", "CommonSchema")
    file.addImport("models.graphql", "GraphQLContext")
    file.addImport("models.graphql", "SchemaHelper")
    file.addImport("sangria.schema", "EnumType")
    file.addImport("sangria.schema", "Field")
    file.addImport("sangria.schema", "ListType")
    file.addImport("sangria.schema", "fields")

    file.add(s"""object ${enum.className}Schema extends SchemaHelper("${enum.propertyName}") {""", 1)
    file.add(s"implicit val ${enum.propertyName}EnumType: EnumType[${enum.className}] = CommonSchema.deriveStringEnumeratumType(", 1)
    file.add(s"""name = "${enum.className}",""")
    file.add(s"""description = "An enumeration of ${enum.className} values.",""")
    file.add(s"values = ${enum.className}.values.map(t => t -> t.value).toList")
    file.add(")", -1)
    file.add()
    file.add("val queryFields = fields[GraphQLContext, Unit](Field(", 1)
    file.add(s"""name = "${enum.propertyName}",""")
    file.add(s"fieldType = ListType(${enum.propertyName}EnumType),")
    file.add(s"""resolve = c => traceB(c.ctx, "list")(_ => ${enum.className}.values)""")
    file.add("))", -1)
    file.add("}", -1)

    file
  }
}
