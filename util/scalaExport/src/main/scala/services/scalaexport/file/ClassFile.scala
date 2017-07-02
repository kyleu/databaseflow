package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.Column
import services.scalaexport.ExportHelper

object ClassFile {
  def export(className: String, columns: Seq[Column]) = {
    val file = ScalaFile("models", className)
    file.add(s"case class $className(", 1)
    columns.map { col =>
      col.columnType.requiredImport.foreach { pkg =>
        file.addImport(pkg, col.columnType.asScala)
      }

      val propName = ExportHelper.toScalaIdentifier.convert(col.name)
      val propType = if (col.notNull) { col.columnType.asScala } else { "Option[" + col.columnType.asScala + "]" }
      val propDefault = col.defaultValue.map(v => " = \"" + v + "\"").getOrElse("")
      val propDecl = s"$propName: $propType$propDefault"
      val comma = if (columns.lastOption.contains(col)) { "" } else { "," }

      file.add(propDecl + comma)
    }
    file.add(")", -1)
    file.filename -> file.render()
  }
}
