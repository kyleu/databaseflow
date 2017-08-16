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

  def addMutationFields(model: ExportModel, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    val pkNames = model.pkFields.map(_.propertyName).mkString(", ")
    val pkArgs = model.pkFields.map(pk => model.propertyName + pk.className + "Arg")
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
