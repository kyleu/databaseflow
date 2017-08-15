package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.Column
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
    case Nil => throw new IllegalStateException("No PK.")
    case h :: Nil => h.columnType.asScala
    case cols => "(" + cols.map(_.columnType.asScala).mkString(", ") + ")"
  }

  def addPrimaryKey(model: ExportModel, file: ScalaFile) = if (model.pkColumns.nonEmpty) {
    val pkCols = model.pkColumns.map(c => model.getField(c.name))
    pkCols.foreach(pkCol => pkCol.t.requiredImport.foreach(pkg => file.addImport(pkg, pkCol.t.asScala)))
    file.addImport("sangria.execution.deferred", "HasId")
    val method = if (model.pkColumns.size == 1) {
      pkCols.headOption.map(c => "_." + c.propertyName).getOrElse(throw new IllegalStateException())
    } else {
      "x => (" + pkCols.map(c => "x." + c.propertyName).mkString(", ") + ")"
    }
    file.add(s"implicit val ${model.propertyName}Id = HasId[${model.className}, ${pkType(model.pkColumns)}]($method)")
    file.add()

    file.addImport("sangria.execution.deferred", "Fetcher")
    val fetcherName = s"${model.propertyName}By${pkCols.map(_.className).mkString}Fetcher"
    file.addMarker("fetcher", (file.pkg :+ (model.className + "Schema")).mkString(".") + "." + fetcherName)
    file.add(s"val $fetcherName = Fetcher((_: GraphQLContext, idSeq: Seq[${pkType(model.pkColumns)}]) => ${model.className}Service.getByIdSeq(idSeq))")
    file.add()
  }

  def addPrimaryKeyArguments(model: ExportModel, file: ScalaFile) = if (model.pkColumns.nonEmpty) {
    model.pkColumns.foreach { pkCol =>
      val field = model.getField(pkCol.name)
      val desc = s"Returns the ${model.title} matching the provided ${field.title}."
      file.add(s"""val ${model.propertyName}${field.className}Arg = Argument("${field.propertyName}", ${field.graphQlArgType}, description = "$desc")""")
    }
    file.add()
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
    file.add("fields = fields[GraphQLContext, Unit](", 1)
    file.add("Field(", 1)
    file.add("name = \"update\",")
    file.add(s"""description = Some("Updates the ${model.title} with the provided $pkNames."),""")
    file.add(s"arguments = ${pkArgs.mkString(" :: ")} :: DataFieldSchema.dataFieldsArg :: Nil,")
    file.add(s"fieldType = ${model.propertyName}Type,")
    file.add(s"resolve = c => {", 1)
    file.add("val dataFields = c.args.arg(DataFieldSchema.dataFieldsArg)")
    file.add(s"${model.className}Service.update($argProps, dataFields)")
    file.add("}", -1)
    file.add("),", -1)

    file.add("Field(", 1)
    file.add("name = \"remove\",")
    file.add(s"""description = Some("Removes the Note with the provided id."),""")
    file.add(s"arguments = ${pkArgs.mkString(" :: ")} :: Nil,")
    file.add(s"fieldType = ${model.propertyName}Type,")
    file.add(s"resolve = c => ${model.className}Service.remove($argProps)")
    file.add(")", -1)

    file.add(")", -1)
    file.add(")", -1)

    file.add()
    val fields = "fields[GraphQLContext, Unit]"
    file.add(s"""val mutationFields = $fields(Field(name = "${model.propertyName}", fieldType = ${model.propertyName}MutationType, resolve = _ => ()))""")
  }
}
