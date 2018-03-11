package services.scalaexport.file.thrift

import models.scalaexport.ScalaFile
import models.scalaexport.thrift.{ThriftMetadata, ThriftService}

object ThriftServiceFile {
  def export(srcPkg: Seq[String], tgtPkg: Seq[String], svc: ThriftService, metadata: ThriftMetadata) = {
    val file = ScalaFile(tgtPkg, svc.name)

    file.addImport("scala.concurrent", "Future")
    file.addImport("_root_.util.tracing", "TraceData")
    file.addImport("_root_.util.tracing", "TracingService")
    file.addImport("_root_.util.FutureUtils", "toScalaFuture")
    file.addImport(s"${srcPkg.mkString(".")}.${svc.name}", "MethodPerEndpoint")

    ThriftOverrides.imports.get(svc.name).foreach(_.foreach(i => file.addImport(i._1, i._2)))

    file.add(s"object ${svc.name} extends models.thrift.ThriftService(", 1)
    file.add(s"""key = "${svc.name}",""")
    file.add(s"route = controllers.admin.thrift.${svc.identifier}.routes.${svc.name}Controller.list()")
    file.add(")", -1)
    file.add()
    file.add("@javax.inject.Singleton")
    file.add(s"class ${svc.name} @javax.inject.Inject() (tracing: TracingService, svc: MethodPerEndpoint) {", 1)
    file.add(s"""private[this] def trace[A](key: String)(f: TraceData => Future[A])(implicit td: TraceData) = tracing.trace("thrift.${svc.name}." + key)(f)""")

    addMethods(svc, metadata, file)

    file.add()
    file.add("""def healthcheck(implicit td: TraceData): Future[String] = trace("getStatus") { _ =>""", 1)
    file.add("Future.successful(svc match {", 1)
    file.add(s"""case thriftSvc: ${srcPkg.mkString(".")}.${svc.name}.FinagledClient => s"${svc.name}: OK"""")
    file.add(s"""case _ => throw new IllegalStateException(s"Cannot query status of [$$svc].")""")
    file.add("})", -1)
    file.add("}", -1)

    file.add("}", -1)

    file
  }

  private[this] def addMethods(svc: ThriftService, metadata: ThriftMetadata, file: ScalaFile) = {
    svc.methods.foreach { method =>
      val args = method.arguments.map { a =>
        val colType = ThriftFileHelper.columnTypeFor(a.t, metadata)._1
        ThriftFileHelper.declarationFor(required = a.required, name = a.name, value = a.value, metadata = metadata, colType = colType)
      }.mkString(", ")
      val retType = ThriftFileHelper.columnTypeFor(method.returnValue, metadata)._1
      file.add()
      file.add(s"""def ${method.name}($args)(implicit td: TraceData): Future[$retType] = trace("${method.name}") { _ =>""", 1)
      val argsMapped = method.arguments.map(arg => ThriftMethodHelper.getArgCall(arg, metadata)).mkString(", ")
      file.add(s"svc.${method.name}($argsMapped)${ThriftMethodHelper.getReturnMapping(retType)}")
      file.add("}", -1)
    }
  }
}
