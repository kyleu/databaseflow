package services.scalaexport.db.file

import models.scalaexport.db.ExportModel
import models.scalaexport.file.ScalaFile

object SchemaHelper {
  def addImports(file: ScalaFile) = {
    file.addImport("models.graphql", "GraphQLContext")
    file.addImport("models.graphql", "SchemaHelper")
    file.addImport("sangria.macros.derive", "_")
    file.addImport("sangria.schema", "_")
    file.addImport("models.graphql.CommonSchema", "_")
    file.addImport("models.graphql.DateTimeSchema", "_")
    file.addImport("models.result.filter.FilterSchema", "_")
    file.addImport("models.result.orderBy.OrderBySchema", "_")
    file.addImport("models.result.paging.PagingSchema", "pagingOptionsType")
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
    model.pkFields.foreach { pkField =>
      val desc = s"Returns the ${model.title} matching the provided ${pkField.title}."
      file.add(s"""val ${model.propertyName}${pkField.className}Arg = Argument("${pkField.propertyName}", ${pkField.graphQlArgType})""")
    }
    model.pkFields match {
      case pkField :: Nil =>
        val desc = s"Returns the ${model.plural} matching the provided primary keys."
        val arg = s"""Argument("${pkField.propertyName}s", ${pkField.graphQlSeqArgType})"""
        file.add(s"""val ${model.propertyName}${pkField.className}SeqArg = $arg""")
      case _ => // noop
    }
    file.add()
  }
}
