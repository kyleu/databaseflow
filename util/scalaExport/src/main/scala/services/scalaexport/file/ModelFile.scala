package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.config.ExportModel

object ModelFile {
  def export(model: ExportModel) = {
    val root = if (model.scalaJs) { Some(ScalaFile.sharedSrc) } else { None }
    val file = ScalaFile(model.modelPackage, model.className, root = root)

    file.addImport("models.result.data", "DataField")
    file.addImport("models.result.data", "DataSummary")
    file.addImport("models.result.data", "DataFieldModel")
    file.addImport("io.circe", "Encoder")
    file.addImport("io.circe", "Decoder")
    file.addImport("io.circe.generic.semiauto", "_")
    if (model.fields.exists(_.t == ColumnType.TimestampType)) {
      file.addImport("io.circe.java8.time", "_")
    }
    file.add(s"object ${model.className} {", 1)
    file.add(s"implicit val jsonEncoder: Encoder[${model.className}] = deriveEncoder")
    file.add(s"implicit val jsonDecoder: Decoder[${model.className}] = deriveDecoder")
    file.add()
    file.add(s"val empty = ${model.className}(", 1)

    model.fields.foreach { field =>
      val withOption = if (field.notNull) {
        field.defaultString
      } else {
        s"Some(${field.defaultString})"
      }

      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }

      file.add(s"${field.propertyName} = $withOption$comma")
    }
    file.add(")", -1)
    file.add("}", -1)
    file.add()

    model.description.foreach(d => file.add(s"/** $d */"))

    if (model.scalaJs) {
      //file.add(s"""@scala.scalajs.js.annotation.JSExportTopLevel("${model.className}")""")
    }
    file.add(s"case class ${model.className}(", 2)
    addFields(model, file)
    model.extendsClass match {
      case Some(x) => file.add(") extends " + x + " {", -2)
      case None => file.add(") extends DataFieldModel {", -2)
    }
    file.indent(1)
    file.add("override def toDataFields = Seq(", 1)
    model.fields.foreach { field =>
      val x = if (field.notNull) {
        s"""DataField("${field.propertyName}", Some(${field.propertyName}.toString))"""
      } else {
        s"""DataField("${field.propertyName}", ${field.propertyName}.map(_.toString))"""
      }
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      file.add(x + comma)
    }
    file.add(")", -1)
    file.add()
    val title = if (model.summaryFields.isEmpty) {
      model.pkFields.map(f => "$" + f.propertyName).mkString(", ")
    } else {
      model.summaryFields.map(f => "$" + f.propertyName + "").mkString(" / ") + " (" + model.pkFields.map(f => "$" + f.propertyName + "").mkString(", ") + ")"
    }
    val pk = model.pkFields.map(f => f.propertyName + ".toString").mkString(", ")
    file.add(s"""def toSummary = DataSummary(model = "${model.propertyName}", pk = Seq($pk), title = s"$title")""")

    file.add("}", -1)
    file
  }

  private[this] def addFields(model: ExportModel, file: ScalaFile) = model.fields.foreach { field =>
    field.t.requiredImport.foreach(p => file.addImport(p, field.t.asScala))

    val colScala = field.t match {
      case ColumnType.ArrayType => ColumnType.ArrayType.forSqlType(field.sqlTypeName)
      case x => x.asScala
    }
    val propType = if (field.notNull) { colScala } else { "Option[" + colScala + "]" }
    val propDefault = if (field.t == ColumnType.StringType) {
      if (field.notNull) {
        field.defaultValue.map(v => " = \"" + v + "\"").getOrElse("")
      } else {
        field.defaultValue.map(v => " = Some(\"" + v + "\")").getOrElse("")
      }
    } else {
      ""
    }
    val propDecl = s"${field.propertyName}: $propType$propDefault"
    val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
    field.description.foreach(d => file.add("/** " + d + " */"))
    file.add(propDecl + comma)
  }
}
