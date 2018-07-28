package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.file.ScalaFile
import models.schema.ColumnType

object ModelHelper {
  def addFields(providedPrefix: String, model: ExportModel, file: ScalaFile) = model.fields.foreach { field =>
    field.addImport(file, model.modelPackage)

    val scalaJsPrefix = if (model.scalaJs) { "@JSExport " } else { "" }

    val colScala = (field.t match {
      case ColumnType.ArrayType => ColumnType.ArrayType.valForSqlType(field.sqlTypeName)
      case _ => field.scalaType
    }).replaceAllLiterally("util.", providedPrefix + "util.").replaceAllLiterally(s"Seq[${providedPrefix}models.tag", s"Seq[${providedPrefix}models.tag")
    val propType = if (field.notNull) { colScala } else { "Option[" + colScala + "]" }
    val propDecl = s"$scalaJsPrefix${field.propertyName}: $propType"
    val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
    field.description.foreach(d => file.add("/** " + d + " */"))
    file.add(propDecl + comma)
  }

  def addEmpty(providedPrefix: String, model: ExportModel, file: ScalaFile) = {
    val fieldStrings = model.fields.map { field =>
      field.addImport(file, model.modelPackage)

      val colScala = (field.t match {
        case ColumnType.ArrayType => ColumnType.ArrayType.valForSqlType(field.sqlTypeName)
        case _ => field.scalaType
      }).replaceAllLiterally("util.", providedPrefix + "util.").replaceAllLiterally(s"Seq[${providedPrefix}models.tag", s"Seq[${providedPrefix}models.tag")
      val propType = if (field.notNull) { colScala } else { "Option[" + colScala + "]" }
      val propDefault = if (field.notNull) {
        " = " + field.defaultString(providedPrefix)
      } else {
        " = None"
      }
      s"${field.propertyName}: $propType$propDefault"
    }.mkString(", ")
    file.add(s"def empty($fieldStrings) = {", 1)
    file.add(s"${model.className}(${model.fields.map(_.propertyName).mkString(", ")})")
    file.add("}", -1)
  }
}
