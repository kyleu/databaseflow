package services.scalaexport.thrift.file

import models.scalaexport.file.ScalaFile
import models.scalaexport.thrift.ThriftIntegerEnum
import services.scalaexport.ExportHelper

object ThriftIntEnumFile {
  def exportInt(srcPkg: Seq[String], tgtPkg: Seq[String], e: ThriftIntegerEnum, exportModelRoot: Option[String], flags: Set[String]) = {
    val file = ScalaFile(pkg = tgtPkg, key = e.name, root = exportModelRoot)
    flags match {
      case x if x("enumString") => exportIntString(srcPkg, tgtPkg, e, exportModelRoot, file)
      case x if x("enumObj") => exportIntObj(srcPkg, tgtPkg, e, exportModelRoot, file)
      case _ => exportIntInt(srcPkg, tgtPkg, e, exportModelRoot, file)
    }
    file
  }

  def exportIntString(srcPkg: Seq[String], tgtPkg: Seq[String], e: ThriftIntegerEnum, exportModelRoot: Option[String], file: ScalaFile) = {
    file.addImport("enumeratum.values", "StringCirceEnum")
    file.addImport("enumeratum.values", "StringEnum")
    file.addImport("enumeratum.values", "StringEnumEntry")

    file.add(s"sealed abstract class ${e.name}(override val value: String, val i: Int) extends StringEnumEntry {", 1)
    file.add(s"lazy val asThrift = ${srcPkg.mkString(".")}.${e.name}.apply(toString)")
    file.add("}", -1)
    file.add()
    file.add(s"object ${e.name} extends StringEnum[${e.name}] with StringCirceEnum[${e.name}] {", 1)
    e.fields.foreach { f =>
      file.add(s"""case object ${ExportHelper.toClassName(f._1)} extends ${e.name}("${f._1}", ${f._2})""")
    }
    file.add()
    fromThrift(srcPkg, e, file)
    file.add("}", -1)
  }

  def exportIntObj(srcPkg: Seq[String], tgtPkg: Seq[String], e: ThriftIntegerEnum, exportModelRoot: Option[String], file: ScalaFile) = {
    file.addImport("enumeratum.values", "IntEnum")
    file.addImport("enumeratum.values", "IntEnumEntry")
    file.addImport("io.circe", "Encoder")
    file.addImport("io.circe", "Decoder")
    file.addImport("io.circe", "HCursor")
    file.addImport("io.circe", "Json")

    file.add(s"sealed abstract class ${e.name}(override val value: Int, val name: String) extends IntEnumEntry {", 1)
    file.add(s"lazy val asThrift = ${srcPkg.mkString(".")}.${e.name}.apply(value)")
    file.add("}", -1)
    file.add()

    file.add(s"object ${e.name} extends IntEnum[${e.name}] {", 1)
    e.fields.foreach { f =>
      file.add(s"""case object ${ExportHelper.toClassName(f._1)} extends ${e.name}(${f._2}, "${ExportHelper.toClassName(f._1)}")""")
    }
    file.add()

    file.add(s"""implicit val jsonEncoder: Encoder[${e.name}] = (a: ${e.name}) => Json.obj(a.name -> Json.obj())""")
    file.add(s"""implicit val jsonDecoder: Decoder[${e.name}] = (c: HCursor) => Right(fromString(c.keys.flatMap(_.headOption).getOrElse("unknown")))""")

    fromThrift(srcPkg, e, file)
    file.add("}", -1)
  }

  def exportIntInt(srcPkg: Seq[String], tgtPkg: Seq[String], e: ThriftIntegerEnum, exportModelRoot: Option[String], file: ScalaFile) = {
    file.addImport("enumeratum.values", "IntCirceEnum")
    file.addImport("enumeratum.values", "IntEnum")
    file.addImport("enumeratum.values", "IntEnumEntry")

    file.add(s"sealed abstract class ${e.name}(override val value: Int, val name: String) extends IntEnumEntry {", 1)
    file.add(s"lazy val asThrift = ${srcPkg.mkString(".")}.${e.name}.apply(value)")
    file.add("}", -1)
    file.add()
    file.add(s"object ${e.name} extends IntEnum[${e.name}] with IntCirceEnum[${e.name}] {", 1)
    e.fields.foreach { f =>
      file.add(s"""case object ${ExportHelper.toClassName(f._1)} extends ${e.name}(${f._2}, "${f._1}")""")
    }

    fromThrift(srcPkg, e, file)
    file.add("}", -1)
  }

  private[this] def fromThrift(srcPkg: Seq[String], e: ThriftIntegerEnum, file: ScalaFile) = {
    file.add()
    file.add(s"def fromThrift(t: ${srcPkg.mkString(".")}.${e.name}) = ${e.name}.withValue(t.getValue)")
    file.add("override val values = findValues")
    file.add(s"""def fromString(s: String) = s.toLowerCase.replaceAllLiterally("_", "") match {""", 1)
    e.fields.foreach { f =>
      file.add(s"""case "${f._1.toLowerCase.replaceAllLiterally("_", "")}" => ${e.name}.${ExportHelper.toClassName(f._1)}""")
    }
    file.add("}", -1)
  }
}
