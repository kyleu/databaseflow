package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.{ExportHelper, ExportTable}

object ControllerFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("controllers" +: "admin" +: et.pkg, et.className + "Controller")

    val viewPkg = ("views" +: "html" +: "admin" +: et.pkg :+ et.propertyName).mkString(".")

    file.addImport("util", "Application")
    file.addImport("util.FutureUtils", "defaultContext")
    file.addImport("controllers", "BaseController")
    if (et.pkg.isEmpty) {
      file.addImport(s"services", s"${et.className}Service")
    } else {
      file.addImport(s"services.${et.pkg.mkString(".")}", s"${et.className}Service")
    }

    file.add("@javax.inject.Singleton")
    file.add(s"class ${et.className}Controller @javax.inject.Inject() (override val app: Application) extends BaseController {", 1)

    file.add(s"""def list(q: Option[String], limit: Option[Int], offset: Option[Int] = None) = {""", 1)
    file.add(s"""withAdminSession("${et.propertyName}.list") { implicit request =>""", 1)

    file.add("val f = q match {", 1)
    file.add(s"case Some(query) if query.nonEmpty => ${et.className}Service.search(query, None, limit.orElse(Some(100)), offset)")
    file.add(s"case _ => ${et.className}Service.getAll(None, limit.orElse(Some(100)), offset)")
    file.add("}", -1)
    file.add("f.map { users =>", 1)
    file.add(s"Ok($viewPkg.list${et.className}(request.identity, q, users, limit.getOrElse(100), offset.getOrElse(0)))")
    file.add("}", -1)

    file.add("}", -1)
    file.add("}", -1)

    et.pkColumns match {
      case Nil => // noop
      case h :: Nil =>
        val hProp = ExportHelper.toIdentifier(h.name)
        file.add()
        file.add(s"""def view($hProp: ${h.columnType.asScalaFull}) = withAdminSession("${et.propertyName}.view") { implicit request =>""", 1)
        file.add(s"""${et.className}Service.getById($hProp).map {""", 1)
        file.add(s"""case Some(model) => Ok($viewPkg.view${et.className}(request.identity, model))""")
        file.add(s"""case None => NotFound(s"No $hProp found with id [$$$hProp].")""")
        file.add("}", -1)
        file.add("}", -1)
      case _ => // todo
    }
    file.add("}", -1)
    file
  }

}
