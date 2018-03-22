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
    file.addImport("play.api.mvc", "Call")
    file.addImport(pkg.mkString("."), "_")

    file.add("@javax.inject.Singleton")
    file.add(s"class ${svc.name}Controller @javax.inject.Inject() (", 2)
    file.add(s"override val app: Application, svc: ${svc.name}")
    file.add(s""") extends BaseController("${svc.name}") {""", -2)
    file.indent()
    file.add(s"""private[this] val rc = controllers.admin.thrift.${svc.identifier}.routes.${svc.name}Controller""")
    file.add()

    file.add("""def list = withSession("list", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"Future.successful(Ok(views.html.admin.thrift.${svc.identifier}(request.identity)))")
    file.add("}", -1)

    svc.methods.foreach(m => addMethod(svc, m, metadata, file))
    addHelpers(svc, file)

    file.add("}", -1)
    file
  }

  private[this] def addMethod(svc: ThriftService, m: ThriftServiceMethod, metadata: ThriftMetadata, file: ScalaFile) = {
    file.add()

    file.add(s"""def ${m.name} = getHelper(title = "${m.name}", act = rc.${m.name}(), args = ${ThriftControllerArgumentHelper.defaultArgs(m, metadata)})""")

    val argNames = m.arguments.map("\"" + _.name + "\"").mkString(", ")
    val postCall = s"""def ${m.name}Call = postHelper(title = "${m.name}", act = rc.${m.name}(), argNames = Seq($argNames), result = (args, td) => svc.${m.name}("""
    if (argNames.isEmpty) {
      file.add(postCall + ")(td).map(_.asJson))")
    } else {
      file.add(postCall, 1)
      m.arguments.foreach { arg =>
        val argRootType = ThriftFileHelper.columnTypeFor(arg.t, metadata)._1
        val argType = if (arg.required) { argRootType } else { s"Option[$argRootType]" }
        val ex = s"""throw new IllegalStateException(s"[${arg.name}] json [$${args("${arg.name}")}] is not a valid [$argType].")"""
        val comma = if (m.arguments.lastOption.contains(arg)) { "" } else { "," }
        file.add(s"""${arg.name} = args("${arg.name}").as[$argType].getOrElse($ex)$comma""")
      }
      file.add(")(td).map(_.asJson))", -1)
    }
  }

  private[this] def addHelpers(svc: ThriftService, file: ScalaFile) = {
    file.add()
    file.add(s"""private[this] val listCall = ("${svc.name}", rc.list())""")

    file.add(s"""private[this] def getHelper(title: String, act: Call, args: Json) = withSession(title, admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"""Future.successful(render {""", 1)
    file.add("case Accepts.Html() => Ok(views.html.admin.layout.methodCall(", 1)
    file.add(s"user = request.identity, title = title, svc = listCall, args = args, act = act, result = None, error = None, debug = app.config.debug")
    file.add("))", -1)
    file.add("""case Accepts.Json() => Ok(Json.obj("name" -> title.asJson, "arguments" -> args.asJson)).as(JSON)""")
    file.add("})", -1)
    file.add("}", -1)

    file.add(s"""private[this] def postHelper(title: String, act: Call, argNames: Seq[String], result: (Map[String, Json], TraceData) => Future[Json]) = {""", 1)
    file.add("withSession(name, admin = true) { implicit request => implicit td =>", 1)
    file.add("val args = jsonArguments(request.body, argNames: _*)")
    file.add(s"""def ren(res: Option[Json] = None, err: Option[(String, String)] = None) = render {""", 1)
    file.add(s"""case Accepts.Html() => Ok(views.html.admin.layout.methodCall(""", 1)
    file.add(s"""user = request.identity, title = title, svc = listCall, args = Json.obj(args.toSeq: _*), act = act, result = res, error = err, debug = app.config.debug""")
    file.add("""))""", -1)
    file.add(s"""case Accepts.Json() => Ok(res.getOrElse(Json.obj("status" -> s"Error: $${err.map(_._2).getOrElse("Unknown")}".asJson))).as(JSON)""")
    file.add("}", -1)
    val err = "Some((x.getClass.getSimpleName, x.getMessage))"
    file.add(s"""result(args, td).map(res => ren(res = Some(res))).recover { case scala.util.control.NonFatal(x) => ren(err = $err) }""")
    file.add("}", -1)
    file.add("}", -1)
  }
}
