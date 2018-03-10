package services.scalaexport.file.db

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
    file.addImport("io.circe.generic.semiauto", "deriveDecoder")
    file.addImport("io.circe.generic.semiauto", "deriveEncoder")
    if (model.fields.exists(_.t == ColumnType.TimestampType)) {
      file.addImport("io.circe.java8.time", "_")
    }
    if (model.scalaJs) {
      file.addImport("scala.scalajs.js.annotation", "JSExport")
      file.addImport("scala.scalajs.js.annotation", "JSExportTopLevel")
    }

    file.add(s"object ${model.className} {", 1)
    file.add(s"implicit val jsonEncoder: Encoder[${model.className}] = deriveEncoder")
    file.add(s"implicit val jsonDecoder: Decoder[${model.className}] = deriveDecoder")
    file.add("}", -1)
    file.add()

    model.description.foreach(d => file.add(s"/** $d */"))

    if (model.scalaJs) {
      file.add(s"""@JSExportTopLevel(util.Config.projectId + ".${model.className}")""")
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
        val method = if (field.t == ColumnType.StringType) { "" } else { ".toString" }
        s"""DataField("${field.propertyName}", Some(${field.propertyName}$method))"""
      } else {
        val method = field.t match {
          case ColumnType.StringType => ""
          case ColumnType.EnumType => ".map(_.value)"
          case _ => ".map(_.toString)"
        }
        s"""DataField("${field.propertyName}", ${field.propertyName}$method)"""
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
    field.addImport(file, model.modelPackage)

    val scalaJsPrefix = if (model.scalaJs) { "@JSExport " } else { "" }

    val colScala = field.t match {
      case ColumnType.ArrayType => ColumnType.ArrayType.valForSqlType(field.sqlTypeName)
      case _ => field.scalaType
    }
    val propType = if (field.notNull) { colScala } else { "Option[" + colScala + "]" }
    val propDefault = if (field.notNull) {
      " = " + field.defaultString
    } else {
      " = None"
    }
    val propDecl = s"$scalaJsPrefix${field.propertyName}: $propType$propDefault"
    val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
    field.description.foreach(d => file.add("/** " + d + " */"))
    file.add(propDecl + comma)
  }
}
