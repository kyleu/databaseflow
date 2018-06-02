package services.scalaexport.db.file

import models.scalaexport.db.ExportModel
import models.scalaexport.file.ScalaFile

object SchemaMutationHelper {
  def addMutationFields(model: ExportModel, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    val pkNames = model.pkFields.map(_.propertyName).mkString(", ")
    val pkArgs = model.pkFields.map(pk => model.propertyName + pk.className + "Arg")
    val argProps = pkArgs.map(arg => s"c.args.arg($arg)").mkString(", ")

    file.addImport("models.result.data", "DataFieldSchema")

    file.add()
    file.add(s"val ${model.propertyName}MutationType = ObjectType(", 1)
    file.add(s"""name = "${model.className}Mutations",""")
    file.add(s"""description = "Mutations for ${model.plural}.",""")
    file.add("fields = fields(", 1)

    val createDesc = s"Creates a new ${model.title} using the provided fields."
    file.add(s"""unitField(name = "create", desc = Some("$createDesc"), t = OptionType(${model.propertyName}Type), f = (c, td) => {""", 1)
    file.add(s"""c.ctx.${model.serviceReference}.create(c.ctx.creds, c.args.arg(DataFieldSchema.dataFieldsArg))(td)""")
    file.add("}, DataFieldSchema.dataFieldsArg),", -1)

    val updateDesc = s"Updates the ${model.title} with the provided $pkNames."
    file.add(s"""unitField(name = "update", desc = Some("$updateDesc"), t = OptionType(${model.propertyName}Type), f = (c, td) => {""", 1)
    file.add(s"""c.ctx.${model.serviceReference}.update(c.ctx.creds, $argProps, c.args.arg(DataFieldSchema.dataFieldsArg))(td).map(_._1)""")
    file.add(s"}, ${pkArgs.mkString(", ")}, DataFieldSchema.dataFieldsArg),", -1)

    val removeDesc = s"Removes the ${model.title} with the provided id."
    file.add(s"""unitField(name = "remove", desc = Some("$removeDesc"), t = ${model.propertyName}Type, f = (c, td) => {""", 1)
    file.add(s"""c.ctx.${model.serviceReference}.remove(c.ctx.creds, $argProps)(td)""")
    file.add(s"}, ${pkArgs.mkString(", ")})", -1)

    file.add(")", -1)
    file.add(")", -1)

    file.add()
    file.addImport("scala.concurrent", "Future")
    val t = model.propertyName + "MutationType"
    file.add(s"""val mutationFields = fields(unitField(name = "${model.propertyName}", desc = None, t = $t, f = (c, td) => Future.successful(())))""")
  }
}
