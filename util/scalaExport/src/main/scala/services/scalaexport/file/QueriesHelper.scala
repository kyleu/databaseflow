package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.{ExportEngine, ExportModel}

object QueriesHelper {
  def fromRow(engine: ExportEngine, model: ExportModel, file: ScalaFile) = {
    file.add(s"override protected def fromRow(row: Row) = ${model.className}(", 1)
    model.fields.foreach { field =>
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      if (field.notNull) {
        file.add(s"""${field.propertyName} = ${field.t.className}(row, "${field.columnName}")$comma""")
      } else {
        file.add(s"""${field.propertyName} = ${field.t.className}.opt(row, "${field.columnName}")$comma""")
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
        val searchArgs = "orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]"
        file.add(s"""case class GetBy$propCls($propId: ${field.t.asScala}, $searchArgs) extends SeqQuery(""", 1)
        file.add(s"""whereClause = Some(quote("${field.columnName}") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),""")
        file.add(s"limit = limit, offset = offset, values = Seq($propId)")
        file.add(")", -1)
        file.add(s"""case class GetBy${propCls}Seq(${propId}Seq: Seq[${field.t.asScala}]) extends ColSeqQuery(column = "${field.columnName}", values = ${propId}Seq)""")
        file.add()
      case _ => // noop
    }
  }
}
