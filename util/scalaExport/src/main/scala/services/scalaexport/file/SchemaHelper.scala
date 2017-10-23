package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.ExportModel

object SchemaHelper {
  val shittyFetchers = false

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
    model.pkFields.foreach(pkField => pkField.t.requiredImport.foreach(pkg => file.addImport(pkg, pkField.t.asScala)))
    file.addImport("scala.concurrent", "Future")
    file.addImport("sangria.execution.deferred", "HasId")
    val method = if (model.pkFields.size == 1) {
      model.pkFields.headOption.map(f => "_." + f.propertyName).getOrElse(throw new IllegalStateException())
    } else {
      "x => (" + model.pkFields.map(f => "x." + f.propertyName).mkString(", ") + ")"
    }
    file.add(s"implicit val ${model.propertyName}PrimaryKeyId = HasId[${model.className}, ${model.pkType}]($method)")
    file.add(s"private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[${model.pkType}]) = {", 1)
    file.add(s"Future.successful(c.${model.serviceReference}.getByPrimaryKeySeq(c.user, idSeq)(c.trace))")
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
      file.add(s"""val ${model.propertyName}${pkField.className}Arg = Argument("${pkField.propertyName}", ${pkField.graphQlArgType}, description = "$desc")""")
    }
    file.add()
  }
}
