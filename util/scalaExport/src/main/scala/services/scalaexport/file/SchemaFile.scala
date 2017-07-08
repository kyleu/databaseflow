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
    if (et.t.foreignKeys.size > 1) {
      file.addImport("sangria.execution.deferred", "Relation")

      et.t.foreignKeys.foreach { fk =>
        val targetTable = et.s.getTable(fk.targetTable).getOrElse(throw new IllegalStateException(s"Missing table [${fk.targetTable}]."))
        val tgtClassName = ExportHelper.toClassName(targetTable.name)
        fk.references.toList match {
          case h :: Nil =>
            val col = et.t.columns.find(_.name == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
            val typ = if (col.notNull) { col.columnType.asScala } else { s"Option[${col.columnType.asScala}]" }

            val tgtPropName = ExportHelper.toIdentifier(h.source)
            file.add(s"""val ${et.propertyName}By$tgtClassName = Relation[${et.className}, $typ]("by$tgtClassName", x => Seq(x.$tgtPropName))""")
          case _ => // noop
        }
      }
      file.add()
    }

    file.add(s"implicit val ${et.propertyName}Type = deriveObjectType[GraphQLContext, ${et.className}]()")
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
