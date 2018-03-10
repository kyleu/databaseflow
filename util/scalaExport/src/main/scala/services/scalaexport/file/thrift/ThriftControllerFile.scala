package services.scalaexport.file.thrift

import models.scalaexport.ScalaFile
import models.scalaexport.thrift.ThriftService

object ThriftControllerFile {
  def export(pkg: Seq[String], svc: ThriftService) = {
    val file = ScalaFile(Seq("controllers", "admin", "thrift", svc.identifier), svc.name + "Controller")
    val viewHtmlPackage = ("views", "admin", "thrift", svc.identifier)

    file.addImport("models", "Application")
    file.addImport("util.FutureUtils", "defaultContext")
    file.addImport("scala.concurrent", "Future")
    file.addImport("io.circe.syntax", "_")
    file.addImport("controllers", "BaseController")
    file.addImport(pkg.mkString("."), svc.name)

    file.add("@javax.inject.Singleton")
    file.add(s"class ${svc.name}Controller @javax.inject.Inject() (", 2)
    file.add(s"override val app: Application, svc: ${svc.name}")
    file.add(s""") extends BaseController("${svc.name}") {""", -2)
    file.indent(1)
    file.add("""def list = withSession("list", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"""Future.successful(Ok(views.html.admin.thrift.${svc.identifier}.list(request.identity)))""")
    file.add("}", -1)
    file.add("}", -1)
    file
  }
}
