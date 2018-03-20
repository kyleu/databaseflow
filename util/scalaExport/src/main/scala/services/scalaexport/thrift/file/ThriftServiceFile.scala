package services.scalaexport.thrift.file

import models.scalaexport.file.ScalaFile
import models.scalaexport.thrift.{ThriftMetadata, ThriftService}

object ThriftServiceFile {
  def export(srcPkg: Seq[String], tgtPkg: Seq[String], svc: ThriftService, metadata: ThriftMetadata, exportModelRoot: Option[String]) = {
    val file = ScalaFile(pkg = tgtPkg, key = svc.name, root = exportModelRoot)

    file.addImport("scala.concurrent", "Future")
    file.addImport("_root_.util.tracing", "TraceData")
    file.addImport("_root_.util.ThriftFutureUtils", "toScalaFuture")

    file.addImport(s"${srcPkg.mkString(".")}.${svc.name}", "MethodPerEndpoint")

    ThriftOverrides.imports.get(svc.name).foreach(_.foreach(i => file.addImport(i._1, i._2)))

    file.add(s"object ${svc.name} extends _root_.util.thrift.ThriftService(", 1)
    file.add(s"""key = "${svc.name}",""")
    file.add(s"""pkg = "${tgtPkg.mkString(".")}",""")
    file.add(s"""route = "/admin/thrift/${svc.identifier.stripSuffix("Service")}"""")
    file.add(")", -1)
    file.add()
    file.add("@javax.inject.Singleton")
    file.add(s"""class ${svc.name} @javax.inject.Inject() (svc: MethodPerEndpoint) extends _root_.util.thrift.ThriftServiceHelper("${svc.name}") {""", 1)
    addMethods(svc, metadata, file)
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
