package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.Column
import services.scalaexport.ExportHelper
import services.scalaexport.config.ExportConfiguration

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

  def pkType(pkColumns: Seq[Column]) = pkColumns match {
    case Nil => None
    case h :: Nil => Some(h.columnType.asScala)
    case cols => Some("(" + cols.map(_.columnType.asScala).mkString(", ") + ")")
  }

  def addPrimaryKey(model: ExportConfiguration.Model, file: ScalaFile) = model.pkColumns match {
    case Nil => // noop
    case pkCol :: Nil =>
      file.addImport("sangria.execution.deferred", "HasId")
      pkCol.columnType.requiredImport.foreach(pkg => file.addImport(pkg, pkCol.columnType.asScala))
      pkCol.columnType.requiredImport.foreach(x => file.addImport(x, pkCol.columnType.asScala))
      file.add(s"implicit val ${model.propertyName}Id = HasId[${model.className}, ${pkCol.columnType.asScala}](_.${ExportHelper.toIdentifier(pkCol.name)})")
      file.add()

      file.addImport("sangria.execution.deferred", "Fetcher")
      val fetcherName = s"${model.propertyName}By${ExportHelper.toClassName(pkCol.name)}Fetcher"
      val pn = ExportHelper.toIdentifier(pkCol.name) + "Seq"
      file.addMarker("fetcher", (file.pkg :+ (model.className + "Schema")).mkString(".") + "." + fetcherName)
      file.add(s"val $fetcherName = Fetcher((_: GraphQLContext, $pn: Seq[${pkCol.columnType.asScala}]) => ${model.className}Service.getByIdSeq($pn))")
      file.add()
    case pkCols =>
      pkCols.foreach(pkCol => pkCol.columnType.requiredImport.foreach(pkg => file.addImport(pkg, pkCol.columnType.asScala)))
      file.addImport("sangria.execution.deferred", "HasId")
      val method = "x => (" + pkCols.map(c => "x." + ExportHelper.toIdentifier(c.name)).mkString(", ") + ")"
      file.add(s"implicit val ${model.propertyName}Id = HasId[${model.className}, ${pkType(pkCols).getOrElse("String")}]($method)")
      file.add()

      file.addImport("sangria.execution.deferred", "Fetcher")
      val fetcherName = s"${model.propertyName}ByIdFetcher"
      file.addMarker("fetcher", (file.pkg :+ (model.className + "Schema")).mkString(".") + "." + fetcherName)
      file.add(s"val $fetcherName = Fetcher((_: GraphQLContext, idSeq: Seq[${pkType(pkCols).getOrElse("String")}]) => ${model.className}Service.getByIdSeq(idSeq))")
      file.add()
  }
}
