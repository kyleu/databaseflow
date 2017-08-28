package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.ExportModel

object ControllerHelper {
  private[this] val relArgs = "orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int]"

  def writePks(model: ExportModel, file: ScalaFile, viewPkg: String, routesClass: String) = if (model.pkFields.nonEmpty) {
    val viewArgs = model.pkFields.map(x => s"${x.propertyName}: ${x.t.asScalaFull}").mkString(", ")
    val callArgs = model.pkFields.map(x => s"${x.propertyName} = ${x.propertyName}").mkString(", ")
    val getArgs = model.pkFields.map(_.propertyName).mkString(", ")
    val logArgs = model.pkFields.map(x => "$" + x.propertyName).mkString(", ")
    val redirArgs = model.pkFields.map(x => "res." + x.propertyName).mkString(", ")
    file.add()
    file.add(s"""def view($viewArgs) = withSession("view", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"svc.getByPrimaryKey($getArgs).map {", 1)
    file.add("case Some(model) => render {", 1)
    file.add(s"case Accepts.Html() => Ok($viewPkg.${model.propertyName}View(request.identity, model, app.config.debug))")
    file.add("case Accepts.Json() => Ok(model.asJson.spaces2).as(JSON)")
    file.add("}", -1)
    file.add(s"""case None => NotFound(s"No ${model.className} found with $getArgs [$logArgs].")""")
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def editForm($viewArgs) = withSession("edit.form", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"val cancel = ${model.routesClass}.view($getArgs)")
    file.add(s"val call = $routesClass.edit($getArgs)")
    file.add(s"svc.getByPrimaryKey($getArgs).map {", 1)
    file.add(s"case Some(model) => Ok(", 1)
    file.add(s"""$viewPkg.${model.propertyName}Form(request.identity, model, s"${model.title} [$logArgs]", cancel, call, debug = app.config.debug)""")
    file.add(")", -1)
    file.add(s"""case None => NotFound(s"No ${model.className} found with $getArgs [$logArgs].")""")
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def edit($viewArgs) = withSession("edit", admin = true) { implicit request => implicit td =>""", 1)
    file.add("val fields = modelForm(request.body.asFormUrlEncoded)")
    file.add(s"svc.update($callArgs, fields = fields).map { res =>", 1)
    file.add("render {", 1)
    file.add(s"case Accepts.Html() => Redirect($routesClass.view($redirArgs))")
    file.add("case Accepts.Json() => Ok(res.asJson.spaces2).as(JSON)")
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def remove($viewArgs) = withSession("remove", admin = true) { implicit request => implicit td =>""", 1)
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
        file.add(s"""def by$propCls($propId: ${col.t.asScala}, $relArgs) = {""", 1)
        file.add(s"""withSession("get.by.$propId", admin = true) { implicit request => implicit td =>""", 1)
        file.add("val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq")
        file.add(s"svc.getBy$propCls($propId, orderBys, limit, offset).map(models => render {", 1)
        file.add(s"case Accepts.Html() => Ok(${model.viewHtmlPackage.mkString(".")}.${model.propertyName}By$propCls(", 1)
        file.add(s"""request.identity, $propId, models, orderBy, orderAsc, limit.getOrElse(5), offset.getOrElse(0)""")
        file.add("))", -1)
        file.add(s"case Accepts.Json() => Ok(models.asJson.spaces2).as(JSON)")
        file.add(s"""case acceptsCsv() => Ok(svc.csvFor("${model.className} by $propId", 0, models)).as("text/csv")""")
        file.add("})", -1)
        file.add("}", -1)
        file.add("}", -1)
      case _ => // noop
    }
  }
}
