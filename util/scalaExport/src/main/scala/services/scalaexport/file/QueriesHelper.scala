package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.{ExportEngine, ExportModel}

object QueriesHelper {
  def fromRow(engine: ExportEngine, model: ExportModel, file: ScalaFile) = {
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

  def writeForeignKeys(engine: ExportEngine, model: ExportModel, file: ScalaFile) = model.foreignKeys.foreach { fk =>
    fk.references.toList match {
      case h :: Nil =>
        file.addImport("models.result", "ResultFieldHelper")
        file.addImport("models.result.orderBy", "OrderBy")

        val field = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
        field.t.requiredImport.foreach(pkg => file.addImport(pkg, field.t.asScala))
        val propId = field.propertyName
        val propCls = field.className
        file.add(s"""case class CountBy$propCls($propId: ${field.t.asScala}) extends ColCount(column = "${field.columnName}", values = Seq($propId))""")
        val searchArgs = "orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]"
        file.add(s"""case class GetBy$propCls($propId: ${field.t.asScala}, $searchArgs) extends SeqQuery(""", 1)
        file.add(s"""whereClause = Some(quote("${field.columnName}") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, engine, orderBys: _*),""")
        file.add(s"limit = limit, offset = offset, values = Seq($propId)")
        file.add(")", -1)
        val sig = s"GetBy${propCls}Seq(${propId}Seq: Seq[${field.t.asScala}])"
        file.add(s"""case class $sig extends ColSeqQuery(column = "${field.columnName}", values = ${propId}Seq)""")
        file.add()
      case _ => // noop
    }
  }
}
