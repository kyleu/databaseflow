package services.scalaexport.file.thrift

import models.scalaexport.ScalaFile
import models.scalaexport.thrift.{ThriftService, ThriftStruct}

object ThriftServiceFile {
  private[this] val inject = "@javax.inject.Inject() (val tracing: TracingService)"

  def export(
    srcPkg: Seq[String],
    tgtPkg: Seq[String],
    svc: ThriftService,
    structs: Seq[ThriftStruct],
    typedefs: Map[String, String],
    enums: Map[String, String],
    pkgMap: Map[String, Seq[String]]
  ) = {
    val file = ScalaFile(tgtPkg, svc.name)

    file.addImport("util.tracing", "TraceData")
    file.addImport("util.tracing", "TracingService")

    file.add("@javax.inject.Singleton")
    file.add(s"""class ${svc.name} $inject {""", 1)
    addMethods(svc, typedefs, enums, structs, pkgMap, file)
    file.add("}", -1)

    file
  }

  private[this] def addMethods(
    svc: ThriftService,
    typedefs: Map[String, String],
    enums: Map[String, String],
    structs: Seq[ThriftStruct],
    pkgMap: Map[String, Seq[String]],
    file: ScalaFile
  ) = {
    svc.methods.foreach { method =>
      val args = method.arguments.map(a => ThriftFileHelper.declarationFor(
        required = a.required,
        name = a.name,
        value = a.value,
        enums = enums,
        pkgMap = pkgMap,
        colType = ThriftFileHelper.columnTypeFor(a.t, typedefs, pkgMap)._1
      )).mkString(", ")
      file.add(s"def ${method.name}($args)(implicit td: TraceData) = {", 1)
      file.add("// TODO")
      file.add("}", -1)
    }
  }
}
