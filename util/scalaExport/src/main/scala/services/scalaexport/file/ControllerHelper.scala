package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.ExportModel

object ControllerHelper {
  def writePks(model: ExportModel, file: ScalaFile, viewPkg: String, routesClass: String) = if (model.pkFields.nonEmpty) {
    val viewArgs = model.pkFields.map(x => s"${x.propertyName}: ${x.t.asScalaFull}").mkString(", ")
    val callArgs = model.pkFields.map(x => s"${x.propertyName} = ${x.propertyName}").mkString(", ")
    val getArgs = model.pkFields.map(_.propertyName).mkString(", ")
    val logArgs = model.pkFields.map(x => "$" + x.propertyName).mkString(", ")
    file.add()
    file.add(s"""def view($viewArgs) = withSession("${model.propertyName}.view", admin = true) { implicit request =>""", 1)
    file.add(s"svc.getByPrimaryKey($getArgs).map {", 1)
    file.add("case Some(model) => render {", 1)
    file.add(s"case Accepts.Html() => Ok($viewPkg.${model.propertyName}View(request.identity, model))")
    file.add("case Accepts.Json() => Ok(model.asJson.spaces2).as(JSON)")
    file.add("}", -1)
    file.add(s"""case None => NotFound(s"No ${model.className} found with $getArgs [$logArgs].")""")
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def editForm($viewArgs) = withSession("${model.propertyName}.edit.form", admin = true) { implicit request =>""", 1)
    file.add(s"val call = $routesClass.edit($getArgs)")
    file.add(s"svc.getByPrimaryKey($getArgs).map {", 1)
    file.add(s"""case Some(model) => Ok($viewPkg.${model.propertyName}Form(request.identity, model, s"${model.title} [$logArgs]", call))""")
    file.add(s"""case None => NotFound(s"No ${model.className} found with $getArgs [$logArgs].")""")
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def edit($viewArgs) = withSession("${model.propertyName}.edit", admin = true) { implicit request =>""", 1)
    file.add("val fields = modelForm(request.body.asFormUrlEncoded)")
    file.add(s"svc.update($callArgs, fields = fields).map { res =>", 1)
    file.add("Ok(play.twirl.api.Html(fields.toString))")
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def remove($viewArgs) = withSession("${model.propertyName}.remove", admin = true) { implicit request =>""", 1)
    file.add(s"svc.remove($callArgs).map(_ => render {", 1)
    file.add(s"case Accepts.Html() => Redirect($routesClass.list())")
    file.add("""case Accepts.Json() => Ok("{ \"status\": \"removed\" }").as(JSON)""")
    file.add("})", -1)
    file.add("}", -1)
  }

  def writeForeignKeys(model: ExportModel, file: ScalaFile) = model.foreignKeys.foreach { fk =>
    fk.references.toList match {
      case h :: Nil =>
        val col = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
        col.t.requiredImport.foreach(pkg => file.addImport(pkg, col.t.asScala))
        val propId = col.propertyName
        val propCls = col.className

        file.add()
        file.add(s"// By$propCls")
        file.add(s"""def by$propCls($propId: ${col.t.asScala}, orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int]) = {""", 1)
        file.add(s"""withSession("${model.propertyName}.get.by.$propId", admin = true) { implicit request =>""", 1)
        file.add(s"svc.getBy$propCls($propId).map(models => render {", 1)
        file.add(s"case Accepts.Html() => Ok(${model.viewHtmlPackage.mkString(".")}.${model.propertyName}Relations(", 1)
        file.add(s"""request.identity, "$propId", orderBy, orderAsc, models, limit.getOrElse(5), offset.getOrElse(0)""")
        file.add("))", -1)
        file.add(s"case Accepts.Json() => Ok(models.asJson.spaces2).as(JSON)")
        file.add("})", -1)
        file.add("}", -1)
        file.add("}", -1)
      case _ => // noop
    }
  }
}
