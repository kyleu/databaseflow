package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.ExportHelper
import services.scalaexport.config.ExportConfiguration

object ControllerFile {
  def export(model: ExportConfiguration.Model) = {
    val file = ScalaFile("controllers" +: "admin" +: model.pkg, model.className + "Controller")

    val viewPkg = ("views" +: "html" +: "admin" +: model.pkg).mkString(".")
    val modelPkg = ("models" +: model.pkg :+ model.className).mkString(".")

    val routesClass = model.pkg match {
      case Nil => s"controllers.admin.routes.${model.className}Controller"
      case _ => s"controllers.admin.${model.pkg.mkString(".")}.routes.${model.className}Controller"
    }

    file.addImport("util", "Application")
    file.addImport("util.FutureUtils", "defaultContext")
    file.addImport("controllers", "BaseController")
    file.addImport("scala.concurrent", "Future")
    file.addImport("models.result.orderBy", "OrderBy")

    if (model.pkg.isEmpty) {
      file.addImport(s"services", s"${model.className}Service")
    } else {
      file.addImport(s"services.${model.pkg.mkString(".")}", s"${model.className}Service")
    }

    file.add("@javax.inject.Singleton")
    file.add(s"class ${model.className}Controller @javax.inject.Inject() (override val app: Application) extends BaseController {", 1)
    file.add(s"""def createForm = withAdminSession("${model.propertyName}.createForm") { implicit request =>""", 1)
    file.add(s"val call = $routesClass.create()")
    file.add(s"Future.successful(Ok($viewPkg.${model.propertyName}Form(request.identity, $modelPkg.empty, call, isNew = true)))")
    file.add("}", -1)
    file.add()
    file.add(s"""def create = withAdminSession("${model.propertyName}.create") { implicit request =>""", 1)
    file.add("Future.successful(Ok(\"OK\"))")
    file.add("}", -1)
    file.add()
    file.add(s"""def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int]) = {""", 1)
    file.add(s"""withAdminSession("${model.propertyName}.list") { implicit request =>""", 1)
    file.add("val orderBys = orderBy.map(o => OrderBy(col = o, dir = OrderBy.Direction.fromBoolAsc(orderAsc))).toSeq")
    file.add("val f = q match {", 1)
    file.add(s"case Some(query) if query.nonEmpty => ${model.className}Service.searchWithCount(query, Nil, orderBys, limit.orElse(Some(100)), offset)")
    file.add(s"case _ => ${model.className}Service.getAllWithCount(Nil, orderBys, limit.orElse(Some(100)), offset)")
    file.add("}", -1)
    file.add(s"f.map(r => Ok($viewPkg.${model.propertyName}List(", 1)
    file.add("request.identity, q, orderBy, orderAsc, Some(r._1), r._2, limit.getOrElse(100), offset.getOrElse(0)")
    file.add(")))", -1)
    file.add("}", -1)
    file.add("}", -1)

    writePks(model, file, viewPkg, routesClass)

    file.add("}", -1)
    file
  }

  private[this] def writePks(model: ExportConfiguration.Model, file: ScalaFile, viewPkg: String, routesClass: String) = {
    if (model.pkColumns.nonEmpty) {
      val viewArgs = model.pkColumns.map(x => s"${ExportHelper.toIdentifier(x.name)}: ${x.columnType.asScalaFull}").mkString(", ")
      val callArgs = model.pkColumns.map(x => s"${ExportHelper.toIdentifier(x.name)} = ${ExportHelper.toIdentifier(x.name)}").mkString(", ")
      val getArgs = model.pkColumns.map(x => ExportHelper.toIdentifier(x.name)).mkString(", ")
      val logArgs = model.pkColumns.map(x => "$" + ExportHelper.toIdentifier(x.name)).mkString(", ")

      file.add()
      file.add(s"""def view($viewArgs) = withAdminSession("${model.propertyName}.view") { implicit request =>""", 1)
      file.add(s"""${model.className}Service.getById($getArgs).map {""", 1)
      file.add(s"""case Some(model) => Ok($viewPkg.${model.propertyName}View(request.identity, model))""")
      file.add(s"""case None => NotFound(s"No ${model.className} found with $getArgs [$logArgs].")""")
      file.add("}", -1)
      file.add("}", -1)
      file.add()
      file.add(s"""def editForm($viewArgs) = withAdminSession("${model.propertyName}.editForm") { implicit request =>""", 1)
      file.add(s"val call = $routesClass.edit($getArgs)")
      file.add(s"""${model.className}Service.getById($getArgs).map {""", 1)
      file.add(s"""case Some(model) => Ok($viewPkg.${model.propertyName}Form(request.identity, model, call))""")
      file.add(s"""case None => NotFound(s"No ${model.className} found with $getArgs [$logArgs].")""")
      file.add("}", -1)
      file.add("}", -1)
      file.add()
      file.add(s"""def edit($viewArgs) = withAdminSession("${model.propertyName}.edit") { implicit request =>""", 1)
      file.add("val fields = modelForm(request.body.asFormUrlEncoded)")
      file.add(s"""${model.className}Service.update($callArgs, fields = fields).map { res =>""", 1)
      file.add("Ok(play.twirl.api.Html(fields.toString))")
      file.add("}", -1)

      file.add("Future.successful(Ok(play.twirl.api.Html(fields.toString)))")
      file.add("}", -1)
    }
  }
}
