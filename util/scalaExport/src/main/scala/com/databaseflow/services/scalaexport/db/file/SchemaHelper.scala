package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.{ExportField, ExportModel}
import com.databaseflow.models.scalaexport.file.ScalaFile

object SchemaHelper {
  def addImports(providedPrefix: String, file: ScalaFile) = {
    val rp = if (providedPrefix.isEmpty) { "models." } else { providedPrefix }

    file.addImport(rp + "graphql", "GraphQLContext")
    file.addImport(rp + "graphql", "GraphQLSchemaHelper")
    file.addImport("sangria.macros.derive", "_")
    file.addImport("sangria.schema", "_")
    file.addImport(rp + "graphql.CommonSchema", "_")
    file.addImport(rp + "graphql.DateTimeSchema", "_")
    file.addImport(providedPrefix + "models.result.filter.FilterSchema", "_")
    file.addImport(providedPrefix + "models.result.orderBy.OrderBySchema", "_")
    file.addImport(providedPrefix + "models.result.paging.PagingSchema", "pagingOptionsType")
  }

  def addPrimaryKey(model: ExportModel, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    model.pkFields.foreach(_.addImport(file))
    file.addImport("sangria.execution.deferred", "HasId")
    val method = if (model.pkFields.lengthCompare(1) == 0) {
      model.pkFields.headOption.map(f => "_." + f.propertyName).getOrElse(throw new IllegalStateException())
    } else {
      "x => (" + model.pkFields.map(f => "x." + f.propertyName).mkString(", ") + ")"
    }
    val hasId = s"HasId[${model.className}, ${model.pkType}]"
    file.add(s"implicit val ${model.propertyName}PrimaryKeyId: $hasId = $hasId($method)")
    file.add(s"private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[${model.pkType}]) = {", 1)
    file.add(s"c.${model.serviceReference}.getByPrimaryKeySeq(c.creds, idSeq)(c.trace)")
    file.add("}", -1)
    file.addImport("sangria.execution.deferred", "Fetcher")
    val fetcherName = s"${model.propertyName}ByPrimaryKeyFetcher"
    file.addMarker("fetcher", (file.pkg :+ (model.className + "Schema")).mkString(".") + "." + fetcherName)
    file.add(s"val $fetcherName = Fetcher(getByPrimaryKeySeq)")
    file.add()
  }

  def addPrimaryKeyArguments(model: ExportModel, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    model.pkFields.foreach { f =>
      addArgument(model, f, file)
      addSeqArgument(model, f, file)
    }
    file.add()
  }

  def addIndexArguments(model: ExportModel, file: ScalaFile) = {
    val filtered = model.indexedFields.filterNot(model.pkFields.contains)
    filtered.foreach { f =>
      addArgument(model, f, file)
      addSeqArgument(model, f, file)
    }
    if (filtered.nonEmpty) { file.add() }
  }

  def addArgument(model: ExportModel, field: ExportField, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    file.add(s"""val ${model.propertyName}${field.className}Arg = Argument("${field.propertyName}", ${field.graphQlArgType})""")
  }

  def addSeqArgument(model: ExportModel, field: ExportField, file: ScalaFile) = {
    val desc = s"Returns the ${model.plural} matching the provided primary keys."
    val arg = s"""Argument("${field.propertyName}s", ${field.graphQlSeqArgType})"""
    file.add(s"""val ${model.propertyName}${field.className}SeqArg = $arg""")
  }

  def addIndexedFields(model: ExportModel, file: ScalaFile) = {
    model.indexedFields.foreach { field =>
      val comma = if (model.indexedFields.lastOption.contains(field)) { "" } else { "," }
      val listType = s"ListType(${model.propertyName}Type)"
      val arg = s"${model.propertyName}${field.className}Arg"
      val seqArg = s"${model.propertyName}${field.className}SeqArg"

      if (field.unique) {
        val optType = s"OptionType(${model.propertyName}Type)"
        file.add(s"""unitField(name = "${model.propertyName}By${field.className}", desc = None, t = $optType, f = (c, td) => {""", 1)
        file.add(s"""c.ctx.${model.serviceReference}.getBy${field.className}(c.ctx.creds, c.arg($arg))(td).map(_.headOption)""")
        file.add(s"""}, $arg),""", -1)
        file.add(s"""unitField(name = "${model.propertyName}By${field.className}Seq", desc = None, t = $listType, f = (c, td) => {""", 1)
        file.add(s"""c.ctx.${model.serviceReference}.getBy${field.className}Seq(c.ctx.creds, c.arg($seqArg))(td)""")
        file.add(s"""}, $seqArg)$comma""", -1)
      } else {
        file.add(s"""unitField(name = "${model.propertyPlural}By${field.className}", desc = None, t = $listType, f = (c, td) => {""", 1)
        file.add(s"""c.ctx.${model.serviceReference}.getBy${field.className}(c.ctx.creds, c.arg($arg))(td)""")
        file.add(s"""}, $arg),""", -1)
        file.add(s"""unitField(name = "${model.propertyPlural}By${field.className}Seq", desc = None, t = $listType, f = (c, td) => {""", 1)
        file.add(s"""c.ctx.${model.serviceReference}.getBy${field.className}Seq(c.ctx.creds, c.arg($seqArg))(td)""")
        file.add(s"""}, $seqArg)$comma""", -1)
      }
    }
  }
}
