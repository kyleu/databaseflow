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

  def writeForeignKeys(engine: ExportEngine, model: ExportModel, file: ScalaFile) = model.foreignKeys.foreach { fk =>
    fk.references.toList match {
      case h :: Nil =>
        val field = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
        field.t.requiredImport.foreach(pkg => file.addImport(pkg, field.t.asScala))
        val propId = field.propertyName
        val propCls = field.className
        file.add(s"""case class CountBy$propCls($propId: ${field.t.asScala}) extends ColCount(column = "${field.columnName}", values = Seq($propId))""")
        file.add(s"""case class GetBy$propCls($propId: ${field.t.asScala}) extends ColSeqQuery(column = "${field.columnName}", values = Seq($propId))""")
        file.add(s"""case class GetBy${propCls}Seq(${propId}Seq: Seq[${field.t.asScala}]) extends ColSeqQuery(column = "${field.columnName}", values = ${propId}Seq)""")
        file.add()
      case _ => // noop
    }
  }
}
