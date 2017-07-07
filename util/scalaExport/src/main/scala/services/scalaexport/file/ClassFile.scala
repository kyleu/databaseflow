package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.{Column, ColumnType}
import services.scalaexport.ExportHelper

object ClassFile {
  def export(className: String, pkg: Seq[String], columns: Seq[Column]) = {
    val file = ScalaFile("models" +: pkg, className)
    file.add(s"case class $className(", 1)
    columns.map { col =>
      col.columnType.requiredImport.foreach { p =>
        file.addImport(p, col.columnType.asScala)
      }

      val propName = ExportHelper.toScalaIdentifier.convert(col.name)
      val propType = if (col.notNull) { col.columnType.asScala } else { "Option[" + col.columnType.asScala + "]" }
      val propDefault = if (col.columnType == ColumnType.StringType) {
        col.defaultValue.map(v => " = \"" + v + "\"").getOrElse("")
      } else {
        ""
      }
      val propDecl = s"$propName: $propType$propDefault"
      val comma = if (columns.lastOption.contains(col)) { "" } else { "," }

      file.add(propDecl + comma)
    }
    file.add(")", -1)
    val ret = ("models" +: pkg, file.filename, file.render())
    ret
  }
}
