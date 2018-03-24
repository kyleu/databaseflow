package services.scalaexport.db.file

import models.scalaexport.db.{ExportField, ExportModel}
import models.scalaexport.db.config.ExportConfiguration
import models.scalaexport.file.GraphQLFile
import models.schema.ColumnType

object GraphQLQueryFiles {
  def export(config: ExportConfiguration, model: ExportModel) = if (model.pkFields.isEmpty) {
    Seq(exportQuery(config, model), exportCreate(config, model))
  } else {
    val args = model.pkFields.map(f => f.propertyName + ": " + argValFor(f.t)).mkString(", ")
    Seq(exportQuery(config, model), exportCreate(config, model), exportUpdate(config, model, args), exportRemove(config, model, args))
  }

  private[this] def addField(f: ExportField, file: GraphQLFile) = f.t match {
    case ColumnType.TagsType =>
      file.add(f.propertyName + " {", 1)
      file.add("k")
      file.add("v")
      file.add("}", -1)
    case _ => file.add(f.propertyName)
  }

  private[this] def argValFor(t: ColumnType) = t match {
    case ColumnType.UuidType => "\"00000000-0000-0000-0000-000000000000\""
    case ColumnType.IntegerType | ColumnType.LongType => "0"
    case ColumnType.FloatType | ColumnType.DoubleType => "0.0"
    case _ => "\"\""
  }

  private[this] def exportQuery(config: ExportConfiguration, model: ExportModel) = {
    val file = GraphQLFile(model.pkg, model.className)

    file.add(s"# Queries the system for ${model.plural}.")
    file.add(s"query ${model.className} {", 1)
    file.add(model.propertyName + "(", 1)
    file.add("q: null, # Or string literal")
    file.add("""filters: null, # Or filters of type `{ k: "", o: Equal, v: "" }`""")
    file.add("""orderBy: null, # Or orderBy of type `{ col: "", dir: Ascending }`""")
    file.add("limit: null, # Or number")
    file.add("offset: null # Or number")
    file.add(") {", -1)
    file.indent()

    file.add("totalCount")
    file.add("paging {", 1)
    file.add("current")
    file.add("next")
    file.add("itemsPerPage")
    file.add("}", -1)
    file.add("results {", 1)
    model.fields.foreach(f => addField(f, file))
    file.add("}", -1)
    file.add("durationMs")
    file.add("occurred")

    file.add("}", -1)
    file.add("}", -1)

    file
  }

  private[this] def exportCreate(config: ExportConfiguration, model: ExportModel) = {
    val file = GraphQLFile(model.pkg, model.className + ".create")

    file.add(s"# Creates a new ${model.className} entity.")
    file.add(s"mutation ${model.className}Create {", 1)
    file.add(s"${model.propertyName} {", 1)
    file.add(s"""create(fields: [{k: "", v: ""}]) {""", 1)
    model.fields.foreach(f => addField(f, file))
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)

    file
  }

  private[this] def exportUpdate(config: ExportConfiguration, model: ExportModel, args: String) = {
    val file = GraphQLFile(model.pkg, model.className + ".update")

    file.add(s"# Updates a single ${model.className} entity using the provided fields.")
    file.add(s"mutation ${model.className}Update {", 1)
    file.add(s"${model.propertyName} {", 1)
    file.add(s"""update($args, fields: [{k: "", v: ""}]) {""", 1)
    model.fields.foreach(f => addField(f, file))
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)

    file
  }

  private[this] def exportRemove(config: ExportConfiguration, model: ExportModel, args: String) = {
    val file = GraphQLFile(model.pkg, model.className + ".remove")

    file.add(s"# Remove a single ${model.className} entity from the system.")
    file.add(s"mutation ${model.className}Remove {", 1)
    file.add(s"${model.propertyName} {", 1)
    file.add(s"remove($args) {", 1)
    model.fields.foreach(f => addField(f, file))
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)

    file
  }
}
