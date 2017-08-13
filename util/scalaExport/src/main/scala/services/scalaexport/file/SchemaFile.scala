package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.{ExportConfiguration, ExportModel}

object SchemaFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile("models" +: model.pkg, model.className + "Schema")

    file.addImport(("services" +: model.pkg).mkString("."), model.className + "Service")
    file.addImport("util.FutureUtils", "graphQlContext")
    SchemaHelper.addImports(file)

    file.add(s"object ${model.className}Schema {", 1)
    SchemaHelper.addPrimaryKey(model, file)
    SchemaHelper.addPrimaryKeyArguments(model, file)
    ForeignKeysHelper.writeSchema(model, file)
    addObjectType(config, model, file)
    addQueryFields(model, file)
    SchemaHelper.addMutationFields(model, file)
    file.add("}", -1)
    file
  }

  private[this] def addObjectType(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val columnsDescriptions = model.fields.flatMap(col => col.description.map(d => s"""DocumentField("${col.propertyName}", "$d")"""))
    if (columnsDescriptions.isEmpty && model.foreignKeys.isEmpty && model.references.isEmpty) {
      file.add(s"implicit lazy val ${model.propertyName}Type: ObjectType[GraphQLContext, ${model.className}] = deriveObjectType()")
    } else {
      file.add(s"implicit lazy val ${model.propertyName}Type: ObjectType[GraphQLContext, ${model.className}] = deriveObjectType(", 1)
      model.description.foreach {
        case d if columnsDescriptions.isEmpty && model.references.isEmpty => file.add(s"""ObjectTypeDescription("$d")""")
        case d => file.add(s"""ObjectTypeDescription("$d"),""")
      }
      columnsDescriptions.foreach {
        case d if columnsDescriptions.lastOption.contains(d) && model.references.isEmpty => file.add(d)
        case d => file.add(d + ",")
      }
      if (model.foreignKeys.nonEmpty || model.references.nonEmpty) {
        file.add("AddFields(", 1)
      }
      ReferencesHelper.writeFields(config, model, file)
      ForeignKeysHelper.writeFields(config, model, file)
      if (model.foreignKeys.nonEmpty || model.references.nonEmpty) {
        file.add(")", -1)
      }
      file.add(")", -1)
    }
    file.add()
    file.add(s"implicit lazy val ${model.propertyName}ResultType: ObjectType[GraphQLContext, ${model.className}Result] = deriveObjectType()")
    file.add()
  }

  private[this] def addQueryFields(model: ExportModel, file: ScalaFile) = {
    file.add("val queryFields = fields[GraphQLContext, Unit](Field(", 1)
    file.add(s"""name = "${model.propertyName}",""")
    file.add(s"fieldType = ${model.propertyName}ResultType,")

    file.add(s"arguments = queryArg :: reportFiltersArg :: orderBysArg :: limitArg :: offsetArg :: Nil,")
    file.add(s"resolve = c =>")
    file.add("{", 1)

    file.add("val start = util.DateUtils.now")
    file.add("val filters = c.arg(reportFiltersArg).getOrElse(Nil)")
    file.add("val orderBys = c.arg(orderBysArg).getOrElse(Nil)")
    file.add("val limit = c.arg(limitArg)")
    file.add("val offset = c.arg(offsetArg)")

    file.add("val f = c.arg(CommonSchema.queryArg) match {", 1)
    file.add(s"case Some(q) => ${model.className}Service.searchWithCount(q, filters, orderBys, limit, offset)")
    file.add(s"case _ => ${model.className}Service.getAllWithCount(filters, orderBys, limit, offset)")
    file.add("}", -1)

    file.add("f.map { r =>", 1)
    file.add("val paging = PagingOptions.from(r._1, limit, offset)")
    file.add("val durationMs = (System.currentTimeMillis - util.DateUtils.toMillis(start)).toInt")
    file.add(s"${model.className}Result(paging = paging, filters = filters, orderBys = orderBys, totalCount = r._1, results = r._2, durationMs = durationMs)")
    file.add("}", -1)

    file.add("}", -1)

    file.add("))", -1)
  }
}
