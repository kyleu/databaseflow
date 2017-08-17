package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.config.{ExportEngine, ExportModel}

object QueriesHelper {
  def fromRow(engine: ExportEngine, model: ExportModel, file: ScalaFile) = {
    file.add(s"override protected def fromRow(row: Row) = ${model.className}(", 1)
    model.fields.foreach { field =>
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      field.t.requiredImport.foreach { p =>
        file.addImport(p, field.t.asScala)
      }
      val colScala = field.t match {
        case ColumnType.BooleanType if engine == ExportEngine.MySQL => ColumnType.ByteType.asScala
        case ColumnType.ArrayType => ColumnType.ArrayType.forSqlType(field.sqlTypeName)
        case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => s"org.joda.time.${field.t.asScala}"
        case x => x.asScala
      }
      val asType = if (field.notNull) { s"as[$colScala]" } else { s"asOpt[$colScala]" }

      field.t match {
        case ColumnType.BooleanType if engine == ExportEngine.MySQL => if (field.notNull) {
          file.add(s"""${field.propertyName} = row.$asType("${field.columnName}") == 1.toByte$comma""")
        } else {
          file.add(s"""${field.propertyName} = row.$asType("${field.columnName}").map(_ == 1.toByte)$comma""")
        }

        case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => if (field.notNull) {
          file.add(s"""${field.propertyName} = fromJoda(row.$asType("${field.columnName}"))$comma""")
        } else {
          file.add(s"""${field.propertyName} = row.$asType("${field.columnName}").map(fromJoda)$comma""")
        }
        case x => file.add(s"""${field.propertyName} = row.$asType("${field.columnName}")$comma""")
      }
    }
    file.add(")", -1)
  }

  private[this] def boolTransformer(cn: String, nn: Boolean) = if (nn) {
    "(if(o." + cn + ") { 1.toByte } else { 0.toByte })"
  } else {
    "o." + cn + ".map(x => (if(x) { 1.toByte } else { 0.toByte }))"
  }

  def toDataSeq(engine: ExportEngine, model: ExportModel, file: ScalaFile) = {
    file.add(s"override protected def toDataSeq(o: ${model.className}) = Seq[Any](", 1)
    file.add(model.fields.map { field =>
      val cn = field.propertyName
      field.t match {
        case ColumnType.BooleanType if engine == ExportEngine.MySQL => boolTransformer(cn, field.notNull)
        case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => if (field.notNull) { s"toJoda(o.$cn)" } else { s"o.$cn.map(toJoda)" }
        case x => s"o.$cn"
      }
    }.mkString(", "))

    file.add(")", -1)
    file
  }
}
