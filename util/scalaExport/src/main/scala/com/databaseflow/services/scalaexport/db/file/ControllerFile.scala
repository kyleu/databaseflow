package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.file.ScalaFile

object ControllerFile {
  def export(config: ExportConfiguration, model: ExportModel) = if (model.truncatedController) {
    ControllerTruncated.export(config, model)
  } else {
    val file = ScalaFile(model.controllerPackage, model.className + "Controller")
    val viewHtmlPackage = model.viewHtmlPackage.mkString(".")
    file.addImport(config.providedPrefix + "models", "Application")
    file.addImport("controllers.admin", "ServiceController")
    file.addImport("services.audit", "AuditRecordService")
    file.addImport("scala.concurrent", "Future")
    file.addImport(config.providedPrefix + "models.result.orderBy", "OrderBy")
    file.addImport(config.providedPrefix + "util.JsonSerializers", "_")
    file.addImport("play.api.http", "MimeTypes")
    file.addImport(config.providedPrefix + "util.ReftreeUtils", "_")
    file.addImport(model.servicePackage.mkString("."), model.className + "Service")
    file.addImport(model.modelPackage.mkString("."), model.className + "Result")

    file.add("@javax.inject.Singleton")
    file.add(s"class ${model.className}Controller @javax.inject.Inject() (", 2)
    ControllerReferences.refServiceArgs(config, model, file) match {
      case ref if ref.trim.isEmpty => file.add(s"override val app: Application, svc: ${model.className}Service, auditRecordSvc: AuditRecordService")
      case ref =>
        file.add(s"override val app: Application, svc: ${model.className}Service, auditRecordSvc: AuditRecordService,")
        file.add(ref)
    }
    file.add(s") extends ServiceController(svc) {", -2)
    file.indent()
    file.add("import app.contexts.webContext")
    file.add()
    file.add("""def createForm = withSession("create.form", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"val cancel = ${model.routesClass}.list()")
    file.add(s"val call = ${model.routesClass}.create()")
    file.add(s"Future.successful(Ok($viewHtmlPackage.${model.propertyName}Form(", 1)
    file.add(s"""request.identity, ${model.modelClass}(), "New ${model.title}", cancel, call, isNew = true, debug = app.config.debug""")
    file.add(")))", -1)
    file.add("}", -1)
    file.add()
    file.add("""def create = withSession("create", admin = true) { implicit request => implicit td =>""", 1)
    file.add("svc.create(request, modelForm(request.body)).map {", 1)
    if (model.pkFields.isEmpty) {
      file.add("case Some(_) => throw new IllegalStateException(\"No primary key.\")")
    } else {
      val viewArgs = model.pkFields.map("model." + _.propertyName).mkString(", ")
      file.add(s"case Some(model) => Redirect(${model.routesClass}.view($viewArgs))")
    }
    file.add(s"case None => Redirect(${model.routesClass}.list())")

    file.add("}", -1)
    file.add("}", -1)
    file.add()
    val listArgs = "orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int], t: Option[String] = None"
    file.add(s"""def list(q: Option[String], $listArgs) = {""", 1)
    file.add("""withSession("list", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"val startMs = ${config.providedPrefix}util.DateUtils.nowMillis")
    file.add("val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq")
    file.add("searchWithCount(q, orderBys, limit, offset).map(r => renderChoice(t) {", 1)

    file.add(s"case MimeTypes.HTML => Ok($viewHtmlPackage.${model.propertyName}List(", 1)
    file.add("request.identity, Some(r._1), r._2, q, orderBy, orderAsc, limit.getOrElse(100), offset.getOrElse(0)")
    file.add("))", -1)
    file.add(s"case MimeTypes.JSON => Ok(${model.className}Result.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson)")
    file.add(s"""case ServiceController.MimeTypes.csv => csvResponse("${model.className}", svc.csvFor(r._1, r._2))""")
    file.add(s"case ServiceController.MimeTypes.png => Ok(renderToPng(v = r._2)).as(ServiceController.MimeTypes.png)")
    file.add(s"case ServiceController.MimeTypes.svg => Ok(renderToSvg(v = r._2)).as(ServiceController.MimeTypes.svg)")

    file.add("})", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add()
    file.add(s"""def autocomplete(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int]) = {""", 1)
    file.add("""withSession("autocomplete", admin = true) { implicit request => implicit td =>""", 1)
    file.add("val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq")
    file.add("search(q, orderBys, limit, None).map(r => Ok(r.map(_.toSummary).asJson))")
    file.add("}", -1)
    file.add("}", -1)
    ControllerHelper.writeForeignKeys(model, file)
    ControllerHelper.writePks(model, file, viewHtmlPackage, model.routesClass)
    ControllerReferences.write(config, model, file)
    file.add("}", -1)
    file
  }
}
