package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.{ExportHelper, ExportTable}

object ModelFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("models" +: et.pkg, et.className)
    et.t.description.foreach(d => file.add(s"/** $d */"))

    file.add(s"case class ${et.className}(", 1)
    et.t.columns.foreach { col =>
      col.columnType.requiredImport.foreach(p => file.addImport(p, col.columnType.asScala))

      val propName = ExportHelper.toIdentifier(col.name)
      val colScala = col.columnType match {
        case ColumnType.ArrayType => ColumnType.ArrayType.forSqlType(col.sqlTypeName)
        case x => x.asScala
      }
      val propType = if (col.notNull) { colScala } else { "Option[" + colScala + "]" }
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
    et.config.extendModels.get(et.propertyName) match {
      case Some(x) => file.add(") extends " + x, -1)
      case None => file.add(")", -1)
    }
    file
  }
}
