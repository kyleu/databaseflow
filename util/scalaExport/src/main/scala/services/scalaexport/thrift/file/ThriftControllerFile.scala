package services.scalaexport.thrift.file

import models.scalaexport.file.ScalaFile
import models.scalaexport.thrift.{ThriftMetadata, ThriftService, ThriftServiceMethod}

object ThriftControllerFile {
  def export(pkg: Seq[String], svc: ThriftService, metadata: ThriftMetadata) = {
    val file = ScalaFile(Seq("controllers", "admin", "thrift", svc.identifier), svc.name + "Controller")

    file.addImport("controllers", "BaseController")
    file.addImport("io.circe.syntax", "_")
    file.addImport("io.circe", "Json")
    file.addImport("models", "Application")
    file.addImport("scala.concurrent", "Future")
    file.addImport("_root_.util.FutureUtils", "defaultContext")
    file.addImport("_root_.util.web.ControllerUtils", "jsonArguments")
    file.addImport("_root_.util.tracing", "TraceData")
    file.addImport(pkg.mkString("."), "_")

    file.add("@javax.inject.Singleton")
    file.add(s"class ${svc.name}Controller @javax.inject.Inject() (", 2)
    file.add(s"override val app: Application, svc: ${svc.name}")
    file.add(s""") extends BaseController("${svc.name}") {""", -2)
    file.indent(1)
    file.add(s"""private[this] val rc = controllers.admin.thrift.${svc.identifier}.routes.${svc.name}Controller""")
    file.add()

    file.add(s"""private[this] def argsHelper(title: String, act: play.api.mvc.Call, args: Json)(implicit request: Req, traceData: TraceData) = {""", 1)
    val argsCallArgs = s"""user = request.identity, title = title, svc = ("${svc.name}", rc.list()), args = args, act = act, result = None, error = None"""
    file.add(s"""Future.successful(Ok(views.html.admin.layout.methodCall($argsCallArgs, debug = app.config.debug)))""")
    file.add("}", -1)
    file.add()

    val callHelperArgs = "title: String, act: play.api.mvc.Call, args: Map[String, Json], result: Map[String, Json] => Future[Json]"
    file.add(s"""private[this] def callHelper($callHelperArgs)(implicit request: Req, traceData: TraceData) = {""", 1)
    file.add(s"""def ren(res: Option[Json] = None, err: Option[(String, String)] = None) = render {""", 1)
    val callCallArgs = s"""user = request.identity, title = title, svc = ("${svc.name}", rc.list()), args = Json.obj(args.toSeq: _*), act = act, result = res"""
    file.add(s"""case Accepts.Html() => Ok(views.html.admin.layout.methodCall($callCallArgs, error = err, debug = app.config.debug))""")
    file.add(s"""case Accepts.Json() => Ok(res.getOrElse(Json.obj("status" -> s"Error: $${err.map(_._2).getOrElse("Unknown")}".asJson))).as(JSON)""")
    file.add("}", -1)
    val err = "Some((x.getClass.getSimpleName, x.getMessage))"
    file.add(s"""result(args).map(res => ren(res = Some(res))).recover { case scala.util.control.NonFatal(x) => ren(err = $err) }""")
    file.add("}", -1)
    file.add()

    file.add("""def list = withSession("list", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"Future.successful(Ok(views.html.admin.thrift.${svc.identifier}(request.identity)))")
    file.add("}", -1)

    svc.methods.foreach(m => addMethod(svc, m, metadata, file))

    file.add("}", -1)
    file
  }

  private[this] def addMethod(svc: ThriftService, m: ThriftServiceMethod, metadata: ThriftMetadata, file: ScalaFile) = {
    file.add()

    file.add(s"""def ${m.name} = withSession("${m.name}", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"""argsHelper(title = "${m.name}", act = rc.${m.name}(), args = ${ThriftControllerArgumentHelper.defaultArgs(m, metadata)})""")
    file.add("}", -1)

    file.add(s"""def ${m.name}Call = withSession("${m.name}", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"""val args = jsonArguments(request.body${m.arguments.map(", \"" + _.name + "\"").mkString})""")

    if (m.arguments.isEmpty) {
      file.add(s"""callHelper(title = "${m.name}", act = rc.${m.name}(), args = args, result = args => svc.${m.name}().map(_.asJson))""")
    } else {
      file.add(s"""callHelper(title = "${m.name}", act = rc.${m.name}(), args = args, result = args => svc.${m.name}(""", 1)
      m.arguments.foreach { arg =>
        val argRootType = ThriftFileHelper.columnTypeFor(arg.t, metadata)._1
        val argType = if (arg.required) { argRootType } else { s"Option[$argRootType]" }
        val ex = s"""throw new IllegalStateException(s"[${arg.name}] json [$${args("${arg.name}")}] is not a valid [$argType].")"""
        val comma = if (m.arguments.lastOption.contains(arg)) { "" } else { "," }
        file.add(s"""${arg.name} = args("${arg.name}").as[$argType].getOrElse($ex)$comma""")
      }
      file.add(").map(_.asJson))", -1)
    }

    file.add("}", -1)
  }
}
