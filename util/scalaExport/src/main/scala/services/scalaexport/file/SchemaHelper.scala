package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.Column
import services.scalaexport.ExportHelper
import services.scalaexport.config.ExportModel

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

  def addPrimaryKey(model: ExportModel, file: ScalaFile) = model.pkColumns match {
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
      val idType = pkType(pkCols).getOrElse("String")
      file.add(s"val $fetcherName = Fetcher((_: GraphQLContext, idSeq: Seq[$idType]) => ${model.className}Service.getByIdSeq(idSeq))")
      file.add()
  }

  def addPrimaryKeyArguments(model: ExportModel, file: ScalaFile) = model.pkColumns match {
    case Nil => // noop
    case pkCols => pkCols.foreach { pkCol =>
      val field = model.getField(pkCol.name)
      val desc = s"Returns the ${model.title} matching the provided ${field.title}."
      file.add(s"""val ${model.propertyName}${field.className}Arg = Argument("${field.propertyName}", ${field.graphQlArgType}, description = "$desc")""")
      file.add()
    }
  }

  def addMutationFields(model: ExportModel, file: ScalaFile) = if (model.pkColumns.nonEmpty) {
    val pks = model.pkColumns.map(c => model.getField(c.name))
    val pkNames = pks.map(_.propertyName).mkString(", ")
    val pkArgs = pks.map(pk => model.propertyName + pk.className + "Arg")
    val argProps = pkArgs.map(arg => s"c.args.arg($arg)").mkString(", ")

    file.addImport("models.result.data", "DataFieldSchema")

    file.add()
    file.add(s"val ${model.propertyName}MutationType = ObjectType(", 1)
    file.add(s"""name = "${model.propertyName}",""")
    file.add(s"""description = "Mutations for ${model.plural}.",""")
    file.add("fields = fields[GraphQLContext, Unit](Field(", 1)
    file.add("name = \"update\",")
    file.add(s"""description = Some("Updates the ${model.title} with the provided $pkNames."),""")
    file.add(s"arguments = ${pkArgs.mkString(" :: ")} :: DataFieldSchema.dataFieldsArg :: Nil,")
    file.add(s"fieldType = OptionType(${model.propertyName}Type),")
    file.add(s"resolve = c => ${model.className}Service.update($argProps, c.args.arg(DataFieldSchema.dataFieldsArg)).flatMap { _ =>", 1)
    file.add(s"${model.className}Service.getById($argProps)")
    file.add("}", -1)
    file.add("))", -1)
    file.add(")", -1)

    file.add()
    file.add("val mutationFields = fields[GraphQLContext, Unit](Field(", 1)
    file.add(s"""name = "${model.propertyName}",""")
    file.add(s"fieldType = ${model.propertyName}MutationType,")
    file.add("resolve = _ => ()")
    file.add("))", -1)
  }
}
