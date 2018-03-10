package services.scalaexport.file.thrift

import models.scalaexport.ScalaFile
import models.scalaexport.thrift.{ThriftStruct, ThriftStructField}

object ThriftModelFile {
  def export(
    srcPkg: Seq[String],
    tgtPkg: Seq[String],
    model: ThriftStruct,
    typedefs: Map[String, String],
    enums: Map[String, String],
    pkgMap: Map[String, Seq[String]]
  ) = {
    val file = ScalaFile(tgtPkg, model.name)

    file.addImport("io.circe", "Encoder")
    file.addImport("io.circe", "Decoder")
    file.addImport("io.circe.generic.semiauto", "deriveDecoder")
    file.addImport("io.circe.generic.semiauto", "deriveEncoder")

    file.addImport("models.result.data", "DataField")
    file.addImport("models.result.data", "DataFieldModel")

    ThriftOverrides.imports.get(model.name).foreach(_.foreach(i => file.addImport(i._1, i._2)))

    file.add(s"object ${model.name} {", 1)
    file.add(s"implicit val jsonEncoder: Encoder[${model.name}] = deriveEncoder")
    file.add(s"implicit val jsonDecoder: Decoder[${model.name}] = deriveDecoder")
    file.add()
    file.add(s"def fromThrift(t: ${srcPkg.mkString(".")}.${model.name}) = ${model.name}(", 1)
    model.fields.foreach { field =>
      val out = ThriftOverrides.overrideFor(model.name, field.name) match {
        case Some(over) => over.fromThrift
        case None => ThriftFieldScalaHelper.getFromThrift(field, typedefs, pkgMap)
      }
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      file.add(field.name + " = " + out + comma)
    }
    file.add(")", -1)
    file.add("}", -1)
    file.add()

    file.add(s"case class ${model.name}(", 2)
    addFields(tgtPkg, model.fields, typedefs, enums, pkgMap, file)
    file.add(") extends DataFieldModel {", -2)
    file.indent(1)
    file.add(s"lazy val asThrift = ${srcPkg.mkString(".")}.${model.name}(", 1)
    model.fields.foreach { field =>
      val out = ThriftOverrides.overrideFor(model.name, field.name) match {
        case Some(over) => over.asThrift
        case None => ThriftFieldThriftHelper.getAsThrift(field, typedefs, pkgMap).stripSuffix(".toMap")
      }
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      file.add(field.name + " = " + out + comma)
    }
    file.add(")", -1)
    file.add()
    file.add("override def toDataFields = Seq(", 1)
    model.fields.foreach { field =>
      val ct = ThriftFileHelper.columnTypeFor(field.t, typedefs = typedefs, pkgMap)
      val x = if (field.required) {
        val method = if (ct._1 == "String") { "" } else { ".toString" }
        s"""DataField("${field.name.replaceAllLiterally("`", "")}", Some(${field.name}$method))"""
      } else {
        val method = ct match {
          case ("String", _) => ""
          case (key, _) if key.endsWith("Enum") => ".map(_.value)"
          case _ => ".map(_.toString)"
        }
        s"""DataField("${field.name}", ${field.name}$method)"""
      }
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      file.add(x + comma)
    }
    file.add(")", -1)
    file.add("}", -1)
    file
  }

  private[this] def addFields(
    pkg: Seq[String],
    fields: Seq[ThriftStructField],
    typedefs: Map[String, String],
    enums: Map[String, String],
    pkgMap: Map[String, Seq[String]],
    file: ScalaFile
  ) = fields.foreach { field =>
    //field.addImport(file, model.modelPackage)
    val comma = if (fields.lastOption.contains(field)) { "" } else { "," }
    val colType = ThriftFileHelper.columnTypeFor(field.t, typedefs, pkgMap)
    if (colType._2.nonEmpty && colType._2 != pkg) {
      file.addImport(colType._2.mkString("."), colType._1)
    }
    val decl = ThriftFileHelper.declarationFor(field.required, field.name, field.value, enums, pkgMap, colType._1)
    file.add(decl + comma)
  }
}
