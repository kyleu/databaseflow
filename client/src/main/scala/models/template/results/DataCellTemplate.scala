package models.template.results

import models.query.QueryResult
import models.schema.ColumnType._
import models.template.Icons
import utils.Messages

import scalatags.Text.all._

object DataCellTemplate {
  def cellValue(col: QueryResult.Col, v: Option[String]) = {
    val contentEl = v match {
      case Some(x) => (col.t match {
        case StringType if x.isEmpty => em("empty string")
        case StringType if x.length > 200 => span(title := x.trim)(x.trim.substring(0, 200) + "...")
        case StringType => span(x.trim)
        case IntegerType => span(x)
        case ShortType => span(x)
        case TimestampType => span(x)
        case BooleanType => span(x)
        case BigDecimalType => span(x)
        case LongType => span(x)
        case ByteArrayType => if (x.length > 200) {
          span(title := x.trim)(x.substring(0, 200) + "...")
        } else {
          span(x)
        }
        case _ => span(x)
      }) -> true
      case None => span(title := "Null")("∅") -> false
      // scalastyle:off
      case null => span("null-bug") -> false
      // scalastyle:on
    }
    col.relationTable match {
      case Some(relTable) if contentEl._2 => td(data("v") := v.getOrElse(""))(
        a(
          cls := "query-rel-link theme-text",
          href := s"#table-$relTable::${col.relationColumn.getOrElse("")}=${v.getOrElse("")}",
          title := Messages("query.open.relation", relTable, s"${col.relationColumn.getOrElse("")}=${v.getOrElse("0")}"),
          data("rel-table") := relTable,
          data("rel-col") := col.relationColumn.getOrElse(""),
          data("rel-val") := v.getOrElse("")
        )(i(cls := s"fa ${Icons.relation}")),
        span(cls := "linked-cell")(contentEl._1)
      )
      case _ => td(data("v") := v.getOrElse(""))(contentEl._1)
    }
  }
}
