package services.scalaexport.db.file

import models.scalaexport.db.ExportEnum
import models.scalaexport.file.ScalaFile

object EnumControllerFile {
  def export(enum: ExportEnum) = {
    val file = ScalaFile(enum.controllerPackage, enum.className + "Controller")
    file.addImport(enum.modelPackage.mkString("."), enum.className)

    file.addImport("controllers", "BaseController")
    file.addImport("scala.concurrent", "Future")
    file.addImport("io.circe.syntax", "_")
    file.addImport("util.web.ControllerUtils", "acceptsCsv")
    file.addImport("play.twirl.api", "Html")

    file.add("@javax.inject.Singleton")
    val constructorArgs = "@javax.inject.Inject() (override val app: models.Application)"
    file.add(s"""class ${enum.className}Controller $constructorArgs extends BaseController("${enum.propertyName}") {""", 1)
    file.add("import app.contexts.webContext")
    file.add()
    file.add(s"""def list = withSession("list", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"Future.successful(render {", 1)
    val listArgs = s"""request.identity, "${enum.className}", "explore""""
    file.add(s"case Accepts.Html() => Ok(views.html.admin.layout.listPage($listArgs, ${enum.className}.values.map(x => Html(x.toString))))")
    file.add(s"""case Accepts.Json() => Ok(${enum.className}.values.asJson)""")
    file.add(s"""case acceptsCsv() => Ok(${enum.className}.values.mkString(", ")).as("text/csv")""")
    file.add(s"})", -1)
    file.add("}", -1)
    file.add("}", -1)
    file
  }
}
