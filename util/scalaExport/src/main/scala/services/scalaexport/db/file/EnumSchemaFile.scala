package services.scalaexport.db.file

import models.scalaexport.db.ExportEnum
import models.scalaexport.file.ScalaFile

object EnumSchemaFile {
  def export(rootPrefix: String, enum: ExportEnum) = {
    val file = ScalaFile(enum.modelPackage, enum.className + "Schema", None)

    file.addImport(rootPrefix + "graphql", "CommonSchema")
    file.addImport(rootPrefix + "graphql", "GraphQLContext")
    file.addImport(rootPrefix + "graphql", "GraphQLSchemaHelper")
    file.addImport("sangria.schema", "EnumType")
    file.addImport("sangria.schema", "ListType")
    file.addImport("sangria.schema", "fields")
    file.addImport("scala.concurrent", "Future")

    file.add(s"""object ${enum.className}Schema extends GraphQLSchemaHelper("${enum.propertyName}") {""", 1)
    file.add(s"implicit val ${enum.propertyName}EnumType: EnumType[${enum.className}] = CommonSchema.deriveStringEnumeratumType(", 1)
    file.add(s"""name = "${enum.className}",""")
    file.add(s"values = ${enum.className}.values")
    file.add(")", -1)
    file.add()

    file.add("val queryFields = fields(", 1)
    val r = s"""Future.successful(${enum.className}.values)"""
    file.add(s"""unitField(name = "${enum.propertyName}", desc = None, t = ListType(${enum.propertyName}EnumType), f = (c, td) => $r)""")
    file.add(")", -1)
    file.add("}", -1)

    file
  }
}
