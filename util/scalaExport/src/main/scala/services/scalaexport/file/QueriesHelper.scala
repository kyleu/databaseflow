package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.{ExportModel, ExportReservedNames}

object QueriesHelper {
  def fromRow(model: ExportModel, file: ScalaFile) = {
    file.add(s"override def fromRow(row: Row) = ${model.className}(", 1)
    model.fields.foreach { field =>
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      if (field.notNull) {
        file.add(s"""${field.propertyName} = ${field.classNameForSqlType}(row, "${field.columnName}")$comma""")
      } else {
        file.add(s"""${field.propertyName} = ${field.classNameForSqlType}.opt(row, "${field.columnName}")$comma""")
      }
    }
    file.add(")", -1)
  }

  def writeForeignKeys(model: ExportModel, file: ScalaFile) = {
    val fkCols = model.foreignKeys.flatMap { fk =>
      fk.references match {
        case ref :: Nil => Some(ref.source)
        case _ => None
      }
    }
    val cols = (fkCols ++ model.searchFields.map(_.columnName)).distinct.sorted
    cols.foreach(col => addColQueriesToFile(model, file, col))
  }

  private[this] def addColQueriesToFile(model: ExportModel, file: ScalaFile, col: String) = {
    file.addImport("models.result", "ResultFieldHelper")
    file.addImport("models.result.orderBy", "OrderBy")

    val field = model.fields.find(_.columnName == col).getOrElse(throw new IllegalStateException(s"Missing column [$col]."))
    field.addImport(file)
    val propId = ExportReservedNames.getColumnPropertyId(field.propertyName)
    val propCls = field.className
    file.add(s"""case class CountBy$propCls($propId: ${field.scalaType}) extends ColCount(column = "${field.columnName}", values = Seq($propId))""")
    val searchArgs = "orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None"
    file.add(s"""case class GetBy$propCls($propId: ${field.scalaType}, $searchArgs) extends SeqQuery(""", 1)
    file.add(s"""whereClause = Some(quote("${field.columnName}") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),""")
    file.add(s"limit = limit, offset = offset, values = Seq($propId)")
    file.add(")", -1)
    val sig = s"GetBy${propCls}Seq(${propId}Seq: Seq[${field.scalaType}])"
    file.add(s"""case class $sig extends ColSeqQuery(column = "${field.columnName}", values = ${propId}Seq)""")
    file.add()
  }

}
