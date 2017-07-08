package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.{ExportHelper, ExportTable}

object SchemaFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("models" +: et.pkg, et.className + "Schema")

    file.addImport(("services" +: et.pkg).mkString("."), et.className + "Service")

    file.addImport("models.graphql", "CommonSchema")
    file.addImport("models.graphql", "GraphQLContext")
    file.addImport("sangria.macros.derive", "_")
    file.addImport("sangria.schema", "_")
    file.addImport("models.graphql.CommonSchema", "_")
    file.addImport("models.graphql.DateTimeSchema", "_")

    file.add(s"object ${et.className}Schema {", 1)

    et.pkColumns match {
      case Nil => // noop
      case pkCol :: Nil =>
        pkCol.columnType.requiredImport.foreach(x => file.addImport(x, pkCol.columnType.asScala))
        file.addImport("sangria.execution.deferred", "HasId")
        file.add(s"implicit val ${et.propertyName}Id = HasId[${et.className}, ${pkCol.columnType.asScala}](_.${ExportHelper.toIdentifier(pkCol.name)})")
        file.add()
      case _ => // multiple columns
    }
    ForeignKeysFile.writeSchema(et, file)
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
    if (et.references.nonEmpty) {
      file.add("AddFields(", 1)
      et.references.foreach { ref =>
        if (ref.pkg != et.pkg) {
          file.addImport(("models" +: ref.pkg).mkString("."), ref.cls + "Schema")
        }
        file.addImport(("services" +: ref.pkg).mkString("."), ref.cls + "Service")
        file.add("Field(", 1)
        file.add(s"""name = "${ref.name}",""")
        file.add(s"""fieldType = ListType(${ref.cls}Schema.${ExportHelper.toIdentifier(ref.cls)}Type),""")
        file.add(s"""arguments = CommonSchema.limitArg :: CommonSchema.offsetArg :: Nil,""")
        if (ref.notNull) {
          file.add(s"""resolve = ctx => ${ref.cls}Service.getBy${ExportHelper.toClassName(ref.prop)}(ctx.value.${ref.tgt})""")
        } else {
          file.add(s"resolve = ctx => ctx.value.${ref.tgt}.map { x =>", 1)
          file.add(s"${ref.cls}Service.getBy${ExportHelper.toClassName(ref.prop)}(x)")
          file.add("}.getOrElse(scala.concurrent.Future.successful(Nil))", -1)
        }
        val comma = if (et.references.lastOption.contains(ref)) { "" } else { "," }
        file.add(")" + comma, -1)
      }
      file.add(")", -1)
    }

    file.add(")", -1)
    file.add()
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
    file
  }
}
