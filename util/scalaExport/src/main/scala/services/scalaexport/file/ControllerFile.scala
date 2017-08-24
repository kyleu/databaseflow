package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.ExportModel

object ControllerFile {
  def export(model: ExportModel) = {
    val file = ScalaFile(model.controllerPackage, model.className + "Controller")
    val viewHtmlPackage = model.viewHtmlPackage.mkString(".")
    file.addImport("models", "Application")
    file.addImport("util.FutureUtils", "defaultContext")
    file.addImport("controllers", "BaseController")
    file.addImport("scala.concurrent", "Future")
    file.addImport("models.result.orderBy", "OrderBy")
    file.addImport("io.circe.generic.auto", "_")
    file.addImport("io.circe.syntax", "_")
    file.addImport("io.circe.java8.time", "_")
    file.addImport(model.servicePackage.mkString("."), model.className + "Service")
    file.addImport(model.modelPackage.mkString("."), model.className + "Result")
    file.add("@javax.inject.Singleton")
    val extend = s"""BaseController("${model.propertyName}")"""
    file.add(s"class ${model.className}Controller @javax.inject.Inject() (override val app: Application, svc: ${model.className}Service) extends $extend {", 1)
    file.add(s"""def createForm = withSession("${model.propertyName}.create.form", admin = true) { implicit request =>""", 1)
    file.add(s"val call = ${model.routesClass}.create()")
    val form = s"""$viewHtmlPackage.${model.propertyName}Form(request.identity, ${model.modelClass}.empty, "New ${model.title}", call, isNew = true)"""
    file.add(s"Future.successful(Ok($form))")
    file.add("}", -1)
    file.add()
    file.add(s"""def create = withSession("${model.propertyName}.create", admin = true) { implicit request =>""", 1)
    file.add("val fields = modelForm(request.body.asFormUrlEncoded)")
    file.add("svc.create(fields = fields).map { res =>", 1)
    file.add("Ok(play.twirl.api.Html(fields.toString))")
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int]) = {""", 1)
    file.add(s"""withSession("${model.propertyName}.list", admin = true) { implicit request =>""", 1)
    file.add("val startMs = util.DateUtils.nowMillis")
    file.add("val orderBys = orderBy.map(o => OrderBy(col = o, dir = OrderBy.Direction.fromBoolAsc(orderAsc))).toSeq")
    file.add("val f = q match {", 1)
    file.add("case Some(query) if query.nonEmpty => svc.searchWithCount(query, Nil, orderBys, limit.orElse(Some(100)), offset)")
    file.add("case _ => svc.getAllWithCount(Nil, orderBys, limit.orElse(Some(100)), offset)")
    file.add("}", -1)
    file.add(s"f.map { r =>", 1)
    file.add("render {", 1)
    file.add(s"case Accepts.Html() => Ok($viewHtmlPackage.${model.propertyName}List(", 1)
    file.add("request.identity, q, orderBy, orderAsc, Some(r._1), r._2, limit.getOrElse(100), offset.getOrElse(0)")
    file.add("))", -1)
    file.add(s"case Accepts.Json() => Ok(${model.className}Result.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson.spaces2).as(JSON)")
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    ControllerHelper.writePks(model, file, viewHtmlPackage, model.routesClass)
    ControllerHelper.writeForeignKeys(model, file)
    file.add("}", -1)
    file
  }
}
