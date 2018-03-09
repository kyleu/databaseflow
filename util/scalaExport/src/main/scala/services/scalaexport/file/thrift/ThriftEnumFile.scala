package services.scalaexport.file.thrift

import models.scalaexport.ScalaFile
import models.scalaexport.thrift.{ThriftIntegerEnum, ThriftStringEnum}

object ThriftEnumFile {
  def exportString(srcPkg: Seq[String], tgtPkg: Seq[String], model: ThriftStringEnum) = {
    val file = ScalaFile(tgtPkg, model.name)

    file.addImport("enumeratum", "CirceEnum")
    file.addImport("enumeratum", "Enum")
    file.addImport("enumeratum", "EnumEntry")

    file.add(s"sealed trait ${model.name} extends EnumEntry {", 1)
    file.add(s"lazy val asThrift = ${srcPkg.mkString(".")}.${model.name}.apply(toString)")
    file.add("}", -1)
    file.add()
    file.add(s"object ${model.name} extends Enum[${model.name}] with CirceEnum[${model.name}] {", 1)
    model.values.foreach { v =>
      file.add(s"case object $v extends ${model.name}")
    }
    file.add()

    file.add(s"def fromThrift(t: ${srcPkg.mkString(".")}.${model.name}) = ${model.name}.foo")
    file.add("}", -1)
    file.add()

    file.add("}", -1)
    file
  }

  def exportInt(srcPkg: Seq[String], tgtPkg: Seq[String], model: ThriftIntegerEnum) = {
    val file = ScalaFile(tgtPkg, model.name)

    file.addImport("enumeratum.values", "IntCirceEnum")
    file.addImport("enumeratum.values", "IntEnum")
    file.addImport("enumeratum.values", "IntEnumEntry")

    file.add(s"sealed abstract class ${model.name}(override val value: Int) extends IntEnumEntry {", 1)
    file.add(s"lazy val asThrift = ${srcPkg.mkString(".")}.${model.name}.apply(value)")
    file.add("}", -1)
    file.add()
    file.add(s"object ${model.name} extends IntEnum[${model.name}] with IntCirceEnum[${model.name}] {", 1)
    model.fields.foreach { f =>
      file.add(s"case object ${f._1} extends ${model.name}(${f._2})")
    }
    file.add()

    file.add(s"def fromThrift(t: ${srcPkg.mkString(".")}.${model.name}) = ${model.name}.withValue(t.getValue)")
    file.add("override val values = findValues")
    file.add("}", -1)
    file
  }
}
