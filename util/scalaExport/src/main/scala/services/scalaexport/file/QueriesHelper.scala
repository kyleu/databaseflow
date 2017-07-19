package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.{ExportHelper, ExportTable}

object QueriesHelper {
  def fromRow(et: ExportTable, file: ScalaFile) = {
    file.add(s"override protected def fromRow(row: Row) = ${et.className}(", 1)
    et.t.columns.foreach { col =>
      val comma = if (et.t.columns.lastOption.contains(col)) { "" } else { "," }
      val colScala = col.columnType match {
        case ColumnType.ArrayType => ColumnType.ArrayType.forSqlType(col.sqlTypeName)
        case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => s"org.joda.time.${col.columnType.asScala}"
        case x =>
          x.requiredImport.foreach { p =>
            file.addImport(p, col.columnType.asScala)
          }
          x.asScala
      }
      val asType = if (col.notNull) { s"as[$colScala]" } else { s"asOpt[$colScala]" }

      col.columnType match {
        case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => if (col.notNull) {
          file.add(s"""${ExportHelper.toIdentifier(col.name)} = fromJoda(row.$asType("${col.name}"))$comma""")
        } else {
          file.add(s"""${ExportHelper.toIdentifier(col.name)} = row.$asType("${col.name}").map(fromJoda)$comma""")
        }
        case x => file.add(s"""${ExportHelper.toIdentifier(col.name)} = row.$asType("${col.name}")$comma""")
      }
    }
    file.add(")", -1)
  }

  def toDataSeq(et: ExportTable, file: ScalaFile) = {
    file.add(s"override protected def toDataSeq(o: ${et.className}) = Seq[Any](", 1)
    file.add(et.t.columns.map { col =>
      val cn = ExportHelper.toIdentifier(col.name)
      col.columnType match {
        case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => if (col.notNull) {
          s"toJoda(o.$cn)"
        } else {
          s"o.$cn.map(toJoda)"
        }
        case x => s"o.$cn"
      }
    }.mkString(", "))

    file.add(")", -1)
    file
  }
}
