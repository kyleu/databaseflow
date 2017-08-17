package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.Column
import services.scalaexport.config.{ExportField, ExportModel}

object SchemaHelper {
  def addImports(file: ScalaFile) = {
    file.addImport("models.graphql", "CommonSchema")
    file.addImport("models.graphql", "GraphQLContext")
    file.addImport("sangria.macros.derive", "_")
    file.addImport("sangria.schema", "_")
    file.addImport("models.graphql.CommonSchema", "_")
    file.addImport("models.graphql.DateTimeSchema", "_")
    file.addImport("models.result.filter.FilterSchema", "_")
    file.addImport("models.result.orderBy.OrderBySchema", "_")
    file.addImport("models.result.paging", "PagingOptions")
    file.addImport("models.result.paging.PagingSchema", "pagingOptionsType")
  }

  def pkType(pkFields: Seq[ExportField]) = pkFields match {
    case Nil => throw new IllegalStateException("No PK.")
    case h :: Nil => h.t.asScala
    case cols => "(" + cols.map(_.t.asScala).mkString(", ") + ")"
  }

  def addPrimaryKey(model: ExportModel, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    model.pkFields.foreach(pkField => pkField.t.requiredImport.foreach(pkg => file.addImport(pkg, pkField.t.asScala)))
    file.addImport("sangria.execution.deferred", "HasId")
    val method = if (model.pkFields.size == 1) {
      model.pkFields.headOption.map(f => "_." + f.propertyName).getOrElse(throw new IllegalStateException())
    } else {
      "x => (" + model.pkFields.map(f => "x." + f.propertyName).mkString(", ") + ")"
    }
    file.add(s"val ${model.propertyName}PrimaryKeyId = HasId[${model.className}, ${pkType(model.pkFields)}]($method)")

    file.addImport("sangria.execution.deferred", "Fetcher")
    val fetcherName = s"${model.propertyName}ByPrimaryKeyFetcher"
    file.addMarker("fetcher", (file.pkg :+ (model.className + "Schema")).mkString(".") + "." + fetcherName)
    file.add(s"val $fetcherName = Fetcher.apply { (_: GraphQLContext, values: Seq[${pkType(model.pkFields)}]) =>", 1)
    file.add(s"${model.className}Service.getByPrimaryKeySeq(values)")
    file.add(s"}(${model.propertyName}PrimaryKeyId)", -1)
    file.add()
  }

  def addPrimaryKeyArguments(model: ExportModel, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    model.pkFields.foreach { pkField =>
      val desc = s"Returns the ${model.title} matching the provided ${pkField.title}."
      file.add(s"""val ${model.propertyName}${pkField.className}Arg = Argument("${pkField.propertyName}", ${pkField.graphQlArgType}, description = "$desc")""")
    }
    file.add()
  }
}
