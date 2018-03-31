package services.scalaexport.thrift.file

import models.scalaexport.file.ScalaFile
import models.scalaexport.thrift.{ThriftIntegerEnum, ThriftStringEnum}
import services.scalaexport.ExportHelper

object ThriftEnumFile {
  def exportString(srcPkg: Seq[String], tgtPkg: Seq[String], e: ThriftStringEnum, exportModelRoot: Option[String]) = {
    val file = ScalaFile(pkg = tgtPkg, key = e.name, root = exportModelRoot)

    file.addImport("enumeratum.values", "StringCirceEnum")
    file.addImport("enumeratum.values", "StringEnum")
    file.addImport("enumeratum.values", "StringEnumEntry")

    file.add(s"sealed abstract class ${e.name}(override val value: String) extends StringEnumEntry {", 1)
    file.add(s"lazy val asThrift = ${srcPkg.mkString(".")}.${e.name}.apply(toString)")
    file.add("}", -1)
    file.add()
    file.add(s"object ${e.name} extends Enum[${e.name}] with CirceEnum[${e.name}] {", 1)
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

  def exportInt(srcPkg: Seq[String], tgtPkg: Seq[String], e: ThriftIntegerEnum, exportModelRoot: Option[String]) = {
    val file = ScalaFile(pkg = tgtPkg, key = e.name, root = exportModelRoot)

    file.addImport("enumeratum.values", "IntCirceEnum")
    file.addImport("enumeratum.values", "IntEnum")
    file.addImport("enumeratum.values", "IntEnumEntry")

    file.add(s"sealed abstract class ${e.name}(override val value: Int, name: String) extends IntEnumEntry {", 1)
    file.add(s"lazy val asThrift = ${srcPkg.mkString(".")}.${e.name}.apply(value)")
    file.add("}", -1)
    file.add()
    file.add(s"object ${e.name} extends IntEnum[${e.name}] with IntCirceEnum[${e.name}] {", 1)
    e.fields.foreach { f =>
      file.add(s"""case object ${ExportHelper.toClassName(f._1)} extends ${e.name}(${f._2}, "${f._1}")""")
    }

    file.add()
    file.add(s"def fromThrift(t: ${srcPkg.mkString(".")}.${e.name}) = ${e.name}.withValue(t.getValue)")
    file.add("override val values = findValues")

    file.add()
    file.add(s"""def fromString(s: String) = s.toLowerCase.replaceAllLiterally("_", "") match {""", 1)
    e.fields.foreach { f =>
      file.add(s"""case "${f._1.toLowerCase.replaceAllLiterally("_", "")}" => ${e.name}.${ExportHelper.toClassName(f._1)}""")
    }
    file.add("}", -1)
    file.add("}", -1)

    file
  }
}
