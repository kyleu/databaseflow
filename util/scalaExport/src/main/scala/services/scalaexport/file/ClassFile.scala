package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.{ExportHelper, ExportTable}

object ClassFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("models" +: et.pkg, et.className)
    et.t.description.foreach(d => file.add(s"/** $d */"))
    file.add(s"case class ${et.className}(", 1)
    et.t.columns.foreach { col =>
      col.columnType.requiredImport.foreach { p =>
        file.addImport(p, col.columnType.asScala)
      }

      val propName = ExportHelper.toIdentifier(col.name)
      val propType = if (col.notNull) { col.columnType.asScala } else { "Option[" + col.columnType.asScala + "]" }
      val propDefault = if (col.columnType == ColumnType.StringType) {
        col.defaultValue.map(v => " = \"" + v + "\"").getOrElse("")
      } else {
        ""
      }
      val propDecl = s"$propName: $propType$propDefault"
      val comma = if (et.t.columns.lastOption.contains(col)) { "" } else { "," }
      col.description.foreach(d => file.add("/** " + d + " */"))
      file.add(propDecl + comma)
    }
    file.add(")", -1)
    file
  }
}
