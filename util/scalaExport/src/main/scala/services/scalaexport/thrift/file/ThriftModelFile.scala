package services.scalaexport.thrift.file

import models.scalaexport.file.ScalaFile
import models.scalaexport.thrift.{ThriftMetadata, ThriftStruct, ThriftStructField}

object ThriftModelFile {
  private[this] val includeDataFields = false

  def export(
    srcPkg: Seq[String], tgtPkg: Seq[String], model: ThriftStruct, metadata: ThriftMetadata,
    exportModelRoot: Option[String], overrides: ThriftOverrides
  ) = {
    val file = ScalaFile(pkg = tgtPkg, key = model.name, root = exportModelRoot)

    file.addImport("util.JsonSerializers", "_")

    if (includeDataFields) {
      file.addImport("models.result.data", "DataField")
      file.addImport("models.result.data", "DataFieldModel")
    }

    overrides.imports.get(model.name).foreach(_.foreach(i => file.addImport(i._1, i._2)))

    file.add(s"object ${model.name} {", 1)
    file.add(s"implicit val jsonEncoder: Encoder[${model.name}] = deriveEncoder")
    file.add(s"implicit val jsonDecoder: Decoder[${model.name}] = deriveDecoder")
    file.add()
    file.add(s"def fromThrift(t: ${srcPkg.mkString(".")}.${model.name}) = ${model.name}(", 1)
    model.fields.foreach { field =>
      val out = overrides.overrideFor(model.name, field.name) match {
        case Some(over) => over.fromThrift
        case None => ThriftFieldScalaHelper.getFromThrift(field, metadata)
      }
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      file.add(field.name + " = " + out + comma)
    }
    file.add(")", -1)
    file.add("}", -1)
    file.add()

    file.add(s"final case class ${model.name}(", 2)
    addFields(tgtPkg, model.fields, metadata, file)
    if (includeDataFields) {
      file.add(") extends DataFieldModel {", -2)
    } else {
      file.add(") {", -2)
    }
    file.indent()
    file.add(s"lazy val asThrift = ${srcPkg.mkString(".")}.${model.name}(", 1)
    model.fields.foreach { field =>
      val out = overrides.overrideFor(model.name, field.name) match {
        case Some(over) => over.asThrift
        case None => ThriftFieldThriftHelper.getAsThrift(field, metadata).stripSuffix(".toMap")
      }
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      file.add(field.name + " = " + out + comma)
    }
    file.add(")", -1)
    if (includeDataFields) {
      file.add()
      file.add("override def toDataFields = Seq(", 1)
      model.fields.foreach { field =>
        val ct = ThriftFileHelper.columnTypeFor(field.t, metadata)
        val x = if (field.required || field.value.isDefined) {
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
    }
    file.add("}", -1)
    file
  }

  private[this] def addFields(pkg: Seq[String], fields: Seq[ThriftStructField], metadata: ThriftMetadata, file: ScalaFile) = fields.foreach { field =>
    //field.addImport(file, model.modelPackage)
    val comma = if (fields.lastOption.contains(field)) { "" } else { "," }
    val colType = ThriftFileHelper.columnTypeFor(field.t, metadata)
    if (colType._2.nonEmpty && colType._2 != pkg) {
      file.addImport(colType._2.mkString("."), colType._1.stripPrefix("Seq[").stripPrefix("Set[").stripSuffix("]").stripSuffix("]"))
    }
    val decl = ThriftFileHelper.declarationFor(field.required || field.value.isDefined, field.name, field.value, metadata, colType._1)
    file.add(decl + comma)
  }
}
