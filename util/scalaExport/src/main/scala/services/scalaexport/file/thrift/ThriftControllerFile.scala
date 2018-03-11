package services.scalaexport.file.thrift

import models.scalaexport.ScalaFile
import models.scalaexport.thrift.{ThriftService, ThriftServiceMethod}

object ThriftControllerFile {
  def export(pkg: Seq[String], svc: ThriftService, enums: Map[String, String], typedefs: Map[String, String], pkgMap: Map[String, Seq[String]]) = {
    val file = ScalaFile(Seq("controllers", "admin", "thrift", svc.identifier), svc.name + "Controller")

    file.addImport("controllers", "BaseController")
    file.addImport("io.circe.syntax", "_")
    file.addImport("io.circe", "Json")
    file.addImport("models", "Application")
    file.addImport("scala.concurrent", "Future")
    file.addImport("_root_.util.FutureUtils", "defaultContext")
    file.addImport("_root_.util.web.ControllerUtils", "jsonArguments")
    file.addImport(pkg.mkString("."), "_")

    file.add("@javax.inject.Singleton")
    file.add(s"class ${svc.name}Controller @javax.inject.Inject() (", 2)
    file.add(s"override val app: Application, svc: ${svc.name}")
    file.add(s""") extends BaseController("${svc.name}") {""", -2)
    file.indent(1)
    file.add(s"""private[this] val rc = controllers.admin.thrift.${svc.identifier}.routes.${svc.name}Controller""")
    file.add()
    file.add("""def list = withSession("list", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"Future.successful(Ok(views.html.admin.thrift.${svc.identifier}(request.identity)))")
    file.add("}", -1)

    svc.methods.foreach(m => addMethod(svc, m, enums, typedefs, pkgMap, file))

    file.add("}", -1)
    file
  }

  private[this] def addMethod(
    svc: ThriftService,
    m: ThriftServiceMethod,
    enums: Map[String, String],
    typedefs: Map[String, String],
    pkgMap: Map[String, Seq[String]],
    file: ScalaFile
  ) = {
    file.add()

    def viewCall(args: String = "args", result: String = "None") = {
      val ret = new StringBuilder()
      ret.append(s"Ok(views.html.admin.thrift.methodCall(")
      ret.append("user = request.identity, ")
      ret.append(s"""title = "${m.name}", """)
      ret.append(s"""svc = ("${svc.name}", rc.list()), """)
      ret.append(s"args = $args, ")
      ret.append(s"act = rc.${m.name}Call(), ")
      ret.append(s"result = $result, ")
      ret.append("debug = app.config.debug")
      ret.append("))")
      ret.toString
    }

    file.add(s"""def ${m.name} = withSession("${m.name}", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"val args = ${ThriftControllerArgumentHelper.defaultArgs(m, enums, typedefs)}")
    file.add(s"Future.successful(${viewCall()})")
    file.add("}", -1)

    file.add(s"""def ${m.name}Call = withSession("${m.name}", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"""val args = jsonArguments(request.body${m.arguments.map(", \"" + _.name + "\"").mkString})""")

    if (m.arguments.isEmpty) {
      file.add(s"svc.${m.name}().map(result => render {", 1)
      file.add(s"case Accepts.Html() => ${viewCall("Json.obj(args.toSeq: _*)", "Some(result.asJson)")}")
      file.add("case Accepts.Json() => Ok(result.asJson).as(JSON)")
      file.add("})", -1)
    } else {
      file.add(s"svc.${m.name}(", 1)
      m.arguments.foreach { arg =>
        val comma = if (m.arguments.lastOption.contains(arg)) { "" } else { "," }
        val argRootType = ThriftFileHelper.columnTypeFor(arg.t, typedefs, pkgMap)._1
        val argType = if (arg.required) { argRootType } else { s"Option[$argRootType]" }
        val ex = s"""throw new IllegalStateException(s"Json [$${args("${arg.name}")}] is not a valid [$argType].")"""
        file.add(s"""${arg.name} = args("${arg.name}").as[$argType].getOrElse($ex)$comma""")
      }
      file.add(s").map(result => render {", -1)
      file.indent(1)
      file.add(s"case Accepts.Html() => ${viewCall("Json.obj(args.toSeq: _*)", "Some(result.asJson)")}")
      file.add("case Accepts.Json() => Ok(result.asJson).as(JSON)")
      file.add("})", -1)
    }

    file.add("}", -1)
  }
}
