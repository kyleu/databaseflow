package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.{ExportHelper, ExportTable}

object ModelFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("models" +: et.pkg, et.className)

    file.add(s"object ${et.className} {", 1)
    file.add(s"val empty = ${et.className}(", 1)

    et.t.columns.foreach { col =>
      val propName = ExportHelper.toIdentifier(col.name)

      val value = col.columnType match {
        case ColumnType.UuidType => "UUID.randomUUID"
        case ColumnType.BooleanType => "false"
        case ColumnType.IntegerType => "0"
        case ColumnType.TimestampType => "util.DateUtils.now"
        case ColumnType.DateType => "util.DateUtils.today"
        case ColumnType.BigDecimalType => "BigDecimal(0)"
        case _ => "\"" + col.defaultValue.getOrElse("") + "\""
      }

      val withOption = if (col.notNull) {
        value
      } else {
        s"Some($value)"
      }

      val comma = if (et.t.columns.lastOption.contains(col)) { "" } else { "," }

      file.add(s"$propName = $withOption$comma")
    }
    file.add(")", -1)
    file.add("}", -1)
    file.add()

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
