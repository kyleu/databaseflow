package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.file.ScalaFile

object SchemaMutationHelper {
  def addMutationFields(rootPrefix: String, model: ExportModel, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    val pkNames = model.pkFields.map(_.propertyName).mkString(", ")
    val pkArgs = model.pkFields.map(pk => model.propertyName + pk.className + "Arg")
    val argProps = pkArgs.map(arg => s"c.arg($arg)").mkString(", ")

    file.addImport(rootPrefix + "models.result.data", "DataFieldSchema")

    file.add()
    file.add(s"val ${model.propertyName}MutationType = ObjectType(", 1)
    file.add(s"""name = "${model.className}Mutations",""")
    file.add("fields = fields(", 1)

    file.add(s"""unitField(name = "create", desc = None, t = OptionType(${model.propertyName}Type), f = (c, td) => {""", 1)
    file.add(s"""c.ctx.${model.serviceReference}.create(c.ctx.creds, c.arg(DataFieldSchema.dataFieldsArg))(td)""")
    file.add("}, DataFieldSchema.dataFieldsArg),", -1)

    file.add(s"""unitField(name = "update", desc = None, t = OptionType(${model.propertyName}Type), f = (c, td) => {""", 1)
    file.add(s"""c.ctx.${model.serviceReference}.update(c.ctx.creds, $argProps, c.arg(DataFieldSchema.dataFieldsArg))(td).map(_._1)""")
    file.add(s"}, ${pkArgs.mkString(", ")}, DataFieldSchema.dataFieldsArg),", -1)

    file.add(s"""unitField(name = "remove", desc = None, t = ${model.propertyName}Type, f = (c, td) => {""", 1)
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
