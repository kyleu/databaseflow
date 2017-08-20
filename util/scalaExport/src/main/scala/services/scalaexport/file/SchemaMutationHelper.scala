package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.ExportModel

object SchemaMutationHelper {
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
    file.add("name = \"create\",")
    file.add(s"""description = Some("Creates a new ${model.title} using the provided fields."),""")
    file.add(s"arguments = DataFieldSchema.dataFieldsArg :: Nil,")
    file.add(s"fieldType = OptionType(${model.propertyName}Type),")
    file.add(s"resolve = c => {", 1)
    file.add("val dataFields = c.args.arg(DataFieldSchema.dataFieldsArg)")
    file.add(s"""trace(c.ctx, "create")(tn => c.ctx.${model.serviceReference}.create(dataFields)(tn))""")
    file.add("}", -1)
    file.add("),", -1)

    file.add("Field(", 1)
    file.add("name = \"update\",")
    file.add(s"""description = Some("Updates the ${model.title} with the provided $pkNames."),""")
    file.add(s"arguments = ${pkArgs.mkString(" :: ")} :: DataFieldSchema.dataFieldsArg :: Nil,")
    file.add(s"fieldType = ${model.propertyName}Type,")
    file.add(s"resolve = c => {", 1)
    file.add("val dataFields = c.args.arg(DataFieldSchema.dataFieldsArg)")
    file.add(s"""trace(c.ctx, "update")(tn => c.ctx.${model.serviceReference}.update($argProps, dataFields)(tn))""")
    file.add("}", -1)
    file.add("),", -1)

    file.add("Field(", 1)
    file.add("name = \"remove\",")
    file.add(s"""description = Some("Removes the Note with the provided id."),""")
    file.add(s"arguments = ${pkArgs.mkString(" :: ")} :: Nil,")
    file.add(s"fieldType = ${model.propertyName}Type,")
    file.add(s"""resolve = c => trace(c.ctx, "remove")(tn => c.ctx.${model.serviceReference}.remove($argProps)(tn))""")
    file.add(")", -1)

    file.add(")", -1)
    file.add(")", -1)

    file.add()
    val fields = "fields[GraphQLContext, Unit]"
    file.add(s"""val mutationFields = $fields(Field(name = "${model.propertyName}", fieldType = ${model.propertyName}MutationType, resolve = _ => ()))""")
  }
}
