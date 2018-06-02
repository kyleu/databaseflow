package services.scalaexport.db.file

import models.scalaexport.db.ExportModel
import models.scalaexport.db.config.ExportConfiguration
import models.scalaexport.file.ScalaFile
import services.scalaexport.ExportHelper

object SchemaFile {
  val resultArgs = "paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur"

  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(model.modelPackage, model.className + "Schema")

    file.addImport("util.FutureUtils", "graphQlContext")
    model.fields.foreach(_.enumOpt.foreach { enum =>
      file.addImport(enum.modelPackage.mkString(".") + "." + enum.className + "Schema", s"${enum.propertyName}EnumType")
    })

    if (model.pkColumns.nonEmpty && (!model.pkg.contains("note"))) { file.addImport("models.note", "NoteSchema") }
    SchemaHelper.addImports(file)

    file.add(s"""object ${model.className}Schema extends SchemaHelper("${model.propertyName}") {""", 1)
    SchemaHelper.addPrimaryKey(model, file)
    SchemaHelper.addPrimaryKeyArguments(model, file)
    SchemaForeignKey.writeSchema(config, model, file)
    addObjectType(config, model, file)
    addQueryFields(model, file)
    SchemaMutationHelper.addMutationFields(model, file)
    file.add()
    file.add(s"private[this] def toResult(r: SchemaHelper.SearchResult[${model.className}]) = {", 1)
    file.add(s"${model.className}Result($resultArgs)")
    file.add("}", -1)
    file.add("}", -1)
    file
  }

  private[this] def addObjectType(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val columnsDescriptions = model.fields.flatMap(col => col.description.map(d => s"""DocumentField("${col.propertyName}", "$d")"""))
    val references = model.validReferences(config)
    if (columnsDescriptions.isEmpty && model.foreignKeys.isEmpty && references.isEmpty) {
      file.add(s"implicit lazy val ${model.propertyName}Type: ObjectType[GraphQLContext, ${model.className}] = deriveObjectType()")
    } else {
      file.add(s"implicit lazy val ${model.propertyName}Type: ObjectType[GraphQLContext, ${model.className}] = deriveObjectType(", 1)
      model.description.foreach {
        case d if columnsDescriptions.isEmpty && references.isEmpty && model.foreignKeys.isEmpty => file.add(s"""ObjectTypeDescription("$d")""")
        case d => file.add(s"""ObjectTypeDescription("$d"),""")
      }
      columnsDescriptions.foreach {
        case d if columnsDescriptions.lastOption.contains(d) && references.isEmpty => file.add(d)
        case d => file.add(d + ",")
      }
      if (model.pkColumns.nonEmpty || model.foreignKeys.nonEmpty || references.nonEmpty) {
        file.add("AddFields(", 1)
      }
      SchemaReferencesHelper.writeFields(config, model, file)
      SchemaForeignKey.writeFields(config, model, file)
      if (model.pkColumns.nonEmpty) {
        file.add("Field(", 1)
        file.add("""name = "relatedNotes",""")
        file.add("""fieldType = ListType(NoteSchema.noteType),""")
        val pkArgs = model.pkFields.map(f => "c.value." + f.propertyName).mkString(", ")
        file.add(s"""resolve = c => c.ctx.app.coreServices.notes.getFor(c.ctx.creds, "${model.propertyName}", $pkArgs)(c.ctx.trace)""")
        file.add(")", -1)
      }
      if (model.pkColumns.nonEmpty || model.foreignKeys.nonEmpty || references.nonEmpty) {
        file.add(")", -1)
      }
      file.add(")", -1)
    }
    file.add()
    file.add(s"implicit lazy val ${model.propertyName}ResultType: ObjectType[GraphQLContext, ${model.className}Result] = deriveObjectType()")
    file.add()
  }

  private[this] def addQueryFields(model: ExportModel, file: ScalaFile) = {
    file.add("val queryFields = fields(", 1)

    if (model.pkFields.nonEmpty) {
      val idDesc = s"Retrieves a single ${model.title} using its primary key."
      file.add(s"""unitField(name = "${model.propertyName}", desc = Some("$idDesc"), t = OptionType(${model.propertyName}Type), f = (c, td) => {""", 1)
      val args = model.pkFields.map(pkField => s"${model.propertyName}${pkField.className}Arg")
      file.add(s"""c.ctx.${model.serviceReference}.getByPrimaryKey(c.ctx.creds, ${args.map(a => s"c.args.arg($a)").mkString(", ")})(td)""")
      file.add(s"}, ${args.mkString(", ")}),", -1)
    }

    val sd = s"Searches for ${model.plural} using the provided arguments."
    val sn = ExportHelper.toIdentifier(model.plural)
    file.add(s"""unitField(name = "$sn", desc = Some("$sd"), t = ${model.propertyName}ResultType, f = (c, td) => {""", 1)
    file.add(s"""runSearch(c.ctx.${model.serviceReference}, c, td).map(toResult)""")
    file.add(s"}, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg)", -1)

    file.add(")", -1)
  }
}
