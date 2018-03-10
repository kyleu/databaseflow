package services.scalaexport.file.thrift

import models.scalaexport.ScalaFile
import models.scalaexport.thrift.{ThriftService, ThriftServiceMethod}

object ThriftControllerFile {
  def export(pkg: Seq[String], svc: ThriftService) = {
    val file = ScalaFile(Seq("controllers", "admin", "thrift", svc.identifier), svc.name + "Controller")
    val viewHtmlPackage = s"views.html.admin.thrift.${svc.identifier}"

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
    file.add(s"Future.successful(Ok(views.html.admin.thrift.${svc.identifier}.list(request.identity)))")
    file.add("}", -1)
    svc.methods.foreach(m => addMethod(m, viewHtmlPackage, file))
    file.add("}", -1)
    file
  }

  private[this] def addMethod(m: ThriftServiceMethod, viewPkg: String, file: ScalaFile) = {
    file.add()
    file.add(s"""def ${m.name} = withSession("${m.name}", admin = true) { implicit request => implicit td =>""", 1)
    file.add("val args = \"\"")
    file.add(s"""Future.successful(Ok($viewPkg.${m.name}(request.identity, args, None, app.config.debug)))""")
    file.add("}", -1)
    file.add(s"""def ${m.name}Call = withSession("${m.name}", admin = true) { implicit request => implicit td =>""", 1)
    file.add("val args = \"\"")
    file.add("val result = None")
    file.add(s"""Future.successful(Ok($viewPkg.${m.name}(request.identity, args, result, app.config.debug)))""")
    file.add("}", -1)
  }
}
