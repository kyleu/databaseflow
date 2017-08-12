package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.{ExportHelper, ExportTable}

object ControllerFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("controllers" +: "admin" +: et.pkg, et.className + "Controller")

    val viewPkg = ("views" +: "html" +: "admin" +: et.pkg).mkString(".")
    val modelPkg = ("models" +: et.pkg :+ et.className).mkString(".")

    file.addImport("util", "Application")
    file.addImport("util.FutureUtils", "defaultContext")
    file.addImport("controllers", "BaseController")
    file.addImport("scala.concurrent", "Future")
    file.addImport("models.result.orderBy", "OrderBy")

    if (et.pkg.isEmpty) {
      file.addImport(s"services", s"${et.className}Service")
    } else {
      file.addImport(s"services.${et.pkg.mkString(".")}", s"${et.className}Service")
    }

    file.add("@javax.inject.Singleton")
    file.add(s"class ${et.className}Controller @javax.inject.Inject() (override val app: Application) extends BaseController {", 1)

    file.add(s"""def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int]) = {""", 1)
    file.add(s"""withAdminSession("${et.propertyName}.list") { implicit request =>""", 1)

    file.add("val orderBys = orderBy.map(o => OrderBy(col = o, dir = OrderBy.Direction.fromBoolAsc(orderAsc))).toSeq")
    file.add("val f = q match {", 1)
    file.add(s"case Some(query) if query.nonEmpty => ${et.className}Service.searchWithCount(query, Nil, orderBys, limit.orElse(Some(100)), offset)")
    file.add(s"case _ => ${et.className}Service.getAllWithCount(Nil, orderBys, limit.orElse(Some(100)), offset)")
    file.add("}", -1)
    file.add(s"f.map(r => Ok($viewPkg.${et.propertyName}List(", 1)
    file.add("request.identity, q, orderBy, orderAsc, Some(r._1), r._2, limit.getOrElse(100), offset.getOrElse(0)")
    file.add(")))", -1)

    file.add("}", -1)
    file.add("}", -1)

    et.pkColumns match {
      case Nil => // noop
      case pkCols =>
        val viewArgs = pkCols.map(x => s"${ExportHelper.toIdentifier(x.name)}: ${x.columnType.asScalaFull}").mkString(", ")
        val getArgs = pkCols.map(x => ExportHelper.toIdentifier(x.name)).mkString(", ")
        val logArgs = pkCols.map(x => "$" + ExportHelper.toIdentifier(x.name)).mkString(", ")

        file.add()
        file.add(s"""def view($viewArgs) = withAdminSession("${et.propertyName}.view") { implicit request =>""", 1)
        file.add(s"""${et.className}Service.getById($getArgs).map {""", 1)
        file.add(s"""case Some(model) => Ok($viewPkg.${et.propertyName}View(request.identity, model))""")
        file.add(s"""case None => NotFound(s"No ${et.className} found with $getArgs [$logArgs].")""")
        file.add("}", -1)
        file.add("}", -1)

        file.add()
        file.add(s"""def formEdit($viewArgs) = withAdminSession("${et.propertyName}.formEdit") { implicit request =>""", 1)
        file.add(s"""${et.className}Service.getById($getArgs).map {""", 1)
        file.add(s"""case Some(model) => Ok($viewPkg.${et.propertyName}Form(request.identity, model))""")
        file.add(s"""case None => NotFound(s"No ${et.className} found with $getArgs [$logArgs].")""")
        file.add("}", -1)
        file.add("}", -1)
    }

    file.add()
    file.add(s"""def formNew = withAdminSession("${et.propertyName}.formNew") { implicit request =>""", 1)
    file.add(s"Future.successful(Ok($viewPkg.${et.propertyName}Form(request.identity, $modelPkg.empty, isNew = true)))")
    file.add("}", -1)

    file.add("}", -1)
    file
  }
}
