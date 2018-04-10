package services.scalaexport.thrift.file

import models.scalaexport.file.ScalaFile
import models.scalaexport.thrift.{ThriftIntegerEnum, ThriftStringEnum}
import services.scalaexport.ExportHelper

object ThriftStringEnumFile {
  def exportString(srcPkg: Seq[String], tgtPkg: Seq[String], e: ThriftStringEnum, exportModelRoot: Option[String]) = {
    val file = ScalaFile(pkg = tgtPkg, key = e.name, root = exportModelRoot)

    file.addImport("enumeratum.values", "StringCirceEnum")
    file.addImport("enumeratum.values", "StringEnum")
    file.addImport("enumeratum.values", "StringEnumEntry")

    file.add(s"sealed abstract class ${e.name}(override val value: String) extends StringEnumEntry {", 1)
    file.add(s"lazy val asThrift = ${srcPkg.mkString(".")}.${e.name}.apply(toString)")
    file.add("}", -1)
    file.add()
    file.add(s"object ${e.name} extends StringEnum[${e.name}] with StringCirceEnum[${e.name}] {", 1)
    e.values.foreach { v =>
      file.add(s"case object ${ExportHelper.toClassName(v)} extends ${e.name}")
    }
    file.add()

    file.add(s"def fromThrift(t: ${srcPkg.mkString(".")}.${e.name}) = ${e.name}.foo")
    file.add("}", -1)
    file.add()

    file.add("}", -1)
    file
  }
}
