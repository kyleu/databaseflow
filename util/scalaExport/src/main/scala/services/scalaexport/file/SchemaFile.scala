package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.{ExportHelper, ExportTable}

object SchemaFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("models" +: et.pkg, et.className + "Schema")

    file.addImport(("services" +: et.pkg).mkString("."), et.className + "Service")
    SchemaFileHelper.addImports(file)

    file.add(s"object ${et.className}Schema {", 1)

    SchemaFileHelper.addPrimaryKey(et, file)
    ForeignKeysFile.writeSchema(et, file)

    addObjectType(et, file)

    addQueryFields(et, file)
    file
  }

  private[this] def addObjectType(et: ExportTable, file: ScalaFile) = {
    file.add(s"implicit val ${et.propertyName}Type = deriveObjectType[GraphQLContext, ${et.className}](", 1)
    val columnsDescriptions = et.t.columns.flatMap(col => col.description.map(d => s"""DocumentField("${ExportHelper.toIdentifier(col.name)}", "$d")"""))
    et.t.description.foreach {
      case d if columnsDescriptions.isEmpty && et.references.isEmpty => file.add(s"""ObjectTypeDescription("$d")""")
      case d => file.add(s"""ObjectTypeDescription("$d"),""")
    }
    columnsDescriptions.foreach {
      case d if columnsDescriptions.lastOption.contains(d) && et.references.isEmpty => file.add(d)
      case d => file.add(d + ",")
    }
    SchemaFileHelper.addReferences(et, file)
    file.add(")", -1)
    file.add()
  }

  private[this] def addQueryFields(et: ExportTable, file: ScalaFile) = {
    file.add("val queryFields = fields[GraphQLContext, Unit](Field(", 1)
    file.add(s"""name = "${et.propertyName}",""")
    file.add(s"fieldType = ListType(${et.propertyName}Type),")
    file.add(s"arguments = queryArg :: limitArg :: offsetArg :: Nil,")
    file.add(s"resolve = c => c.arg(CommonSchema.queryArg) match {", 1)
    file.add(s"case Some(q) => ${et.className}Service.search(q, None, c.arg(limitArg), c.arg(offsetArg))")
    file.add(s"case _ => ${et.className}Service.getAll(None, c.arg(limitArg), c.arg(offsetArg))")
    file.add("}", -1)
    file.add("))", -1)
    file.add("}", -1)
  }
}
