package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.{ExportHelper, ExportTable}

object SchemaFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("models" +: et.pkg, et.className + "Schema")

    file.addImport(("services" +: et.pkg).mkString("."), et.className + "Service")
    file.addImport("util.FutureUtils", "graphQlContext")
    SchemaHelper.addImports(file)

    file.add(s"object ${et.className}Schema {", 1)

    SchemaHelper.addPrimaryKey(et, file)
    ForeignKeysHelper.writeSchema(et, file)

    addObjectType(et, file)

    addQueryFields(et, file)
    file
  }

  private[this] def addObjectType(et: ExportTable, file: ScalaFile) = {
    val columnsDescriptions = et.t.columns.flatMap(col => col.description.map(d => s"""DocumentField("${ExportHelper.toIdentifier(col.name)}", "$d")"""))
    if (columnsDescriptions.isEmpty && et.t.foreignKeys.isEmpty && et.references.isEmpty) {
      file.add(s"implicit lazy val ${et.propertyName}Type: ObjectType[GraphQLContext, ${et.className}] = deriveObjectType()")
    } else {
      file.add(s"implicit lazy val ${et.propertyName}Type: ObjectType[GraphQLContext, ${et.className}] = deriveObjectType(", 1)
      et.t.description.foreach {
        case d if columnsDescriptions.isEmpty && et.references.isEmpty => file.add(s"""ObjectTypeDescription("$d")""")
        case d => file.add(s"""ObjectTypeDescription("$d"),""")
      }
      columnsDescriptions.foreach {
        case d if columnsDescriptions.lastOption.contains(d) && et.references.isEmpty => file.add(d)
        case d => file.add(d + ",")
      }
      if (et.t.foreignKeys.nonEmpty || et.references.nonEmpty) {
        file.add("AddFields(", 1)
      }
      ReferencesHelper.writeFields(et, file)
      ForeignKeysHelper.writeFields(et, file)
      if (et.t.foreignKeys.nonEmpty || et.references.nonEmpty) {
        file.add(")", -1)
      }
      file.add(")", -1)
    }
    file.add()
    file.add(s"implicit lazy val ${et.propertyName}ResultType: ObjectType[GraphQLContext, ${et.className}Result] = deriveObjectType()")
    file.add()
  }

  private[this] def addQueryFields(et: ExportTable, file: ScalaFile) = {
    file.add("val queryFields = fields[GraphQLContext, Unit](Field(", 1)
    file.add(s"""name = "${et.propertyName}",""")
    file.add(s"fieldType = ${et.propertyName}ResultType,")

    file.add(s"arguments = queryArg :: reportFiltersArg :: orderBysArg :: limitArg :: offsetArg :: Nil,")
    file.add(s"resolve = c =>")
    file.add("{", 1)

    file.add("val start = util.DateUtils.now")
    file.add("val filters = c.arg(reportFiltersArg).getOrElse(Nil)")
    file.add("val orderBys = c.arg(orderBysArg).getOrElse(Nil)")
    file.add("val limit = c.arg(limitArg)")
    file.add("val offset = c.arg(offsetArg)")

    file.add("val f = c.arg(CommonSchema.queryArg) match {", 1)
    file.add(s"case Some(q) => ${et.className}Service.searchWithCount(q, filters, orderBys, limit, offset)")
    file.add(s"case _ => ${et.className}Service.getAllWithCount(filters, orderBys, limit, offset)")
    file.add("}", -1)

    file.add("f.map { r =>", 1)
    file.add("val paging = PagingOptions.from(r._1, limit, offset)")
    file.add("val durationMs = (System.currentTimeMillis - util.DateUtils.toMillis(start)).toInt")
    file.add(s"${et.className}Result(paging = paging, filters = filters, orderBys = orderBys, totalCount = r._1, records = r._2, durationMs = durationMs)")
    file.add("}", -1)

    file.add("}", -1)

    file.add("))", -1)
    file.add("}", -1)
  }
}
