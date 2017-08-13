package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.ExportHelper
import services.scalaexport.config.ExportConfiguration

object ControllerFile {
  def export(model: ExportConfiguration.Model) = {
    val file = ScalaFile("controllers" +: "admin" +: model.pkg, model.className + "Controller")

    val viewPkg = ("views" +: "html" +: "admin" +: model.pkg).mkString(".")
    val modelPkg = ("models" +: model.pkg :+ model.className).mkString(".")

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

    model.pkColumns match {
      case Nil => // noop
      case pkCols =>
        val viewArgs = pkCols.map(x => s"${ExportHelper.toIdentifier(x.name)}: ${x.columnType.asScalaFull}").mkString(", ")
        val getArgs = pkCols.map(x => ExportHelper.toIdentifier(x.name)).mkString(", ")
        val logArgs = pkCols.map(x => "$" + ExportHelper.toIdentifier(x.name)).mkString(", ")

        file.add()
        file.add(s"""def view($viewArgs) = withAdminSession("${model.propertyName}.view") { implicit request =>""", 1)
        file.add(s"""${model.className}Service.getById($getArgs).map {""", 1)
        file.add(s"""case Some(model) => Ok($viewPkg.${model.propertyName}View(request.identity, model))""")
        file.add(s"""case None => NotFound(s"No ${model.className} found with $getArgs [$logArgs].")""")
        file.add("}", -1)
        file.add("}", -1)

        file.add()
        file.add(s"""def formEdit($viewArgs) = withAdminSession("${model.propertyName}.formEdit") { implicit request =>""", 1)
        file.add(s"""${model.className}Service.getById($getArgs).map {""", 1)
        file.add(s"""case Some(model) => Ok($viewPkg.${model.propertyName}Form(request.identity, model))""")
        file.add(s"""case None => NotFound(s"No ${model.className} found with $getArgs [$logArgs].")""")
        file.add("}", -1)
        file.add("}", -1)
    }

    file.add()
    file.add(s"""def formNew = withAdminSession("${model.propertyName}.formNew") { implicit request =>""", 1)
    file.add(s"Future.successful(Ok($viewPkg.${model.propertyName}Form(request.identity, $modelPkg.empty, isNew = true)))")
    file.add("}", -1)

    file.add("}", -1)
    file
  }
}
