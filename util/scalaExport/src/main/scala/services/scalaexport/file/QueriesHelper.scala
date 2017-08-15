package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.config.ExportModel

object QueriesHelper {
  def fromRow(model: ExportModel, file: ScalaFile) = {
    file.add(s"override protected def fromRow(row: Row) = ${model.className}(", 1)
    model.fields.foreach { field =>
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      field.t.requiredImport.foreach { p =>
        file.addImport(p, field.t.asScala)
      }
      val colScala = field.t match {
        case ColumnType.ArrayType => ColumnType.ArrayType.forSqlType(field.sqlTypeName)
        case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => s"org.joda.time.${field.t.asScala}"
        case x => x.asScala
      }
      val asType = if (field.notNull) { s"as[$colScala]" } else { s"asOpt[$colScala]" }

      field.t match {
        case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => if (field.notNull) {
          file.add(s"""${field.propertyName} = fromJoda(row.$asType("${field.columnName}"))$comma""")
        } else {
          file.add(s"""${field.propertyName} = row.$asType("${field.columnName}").map(fromJoda)$comma""")
        }
        case ColumnType.ByteType => file.add(s"""${field.propertyName} = row.$asType("${field.columnName}").map(_.toInt)$comma""")
        case x => file.add(s"""${field.propertyName} = row.$asType("${field.columnName}")$comma""")
      }
    }
    file.add(")", -1)
  }

  def toDataSeq(model: ExportModel, file: ScalaFile) = {
    file.add(s"override protected def toDataSeq(o: ${model.className}) = Seq[Any](", 1)
    file.add(model.fields.map { field =>
      val cn = field.propertyName
      field.t match {
        case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => if (field.notNull) { s"toJoda(o.$cn)" } else { s"o.$cn.map(toJoda)" }
        case ColumnType.ByteType => if (field.notNull) { s"o.$cn.toByte" } else { s"o.$cn.map(_.toByte)" }
        case x => s"o.$cn"
      }
    }.mkString(", "))

    file.add(")", -1)
    file
  }
}
