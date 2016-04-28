package models.template

import models.query.QueryResult
import models.schema.ColumnType._

import scalatags.Text.all._

object DataTableTemplate {
  private[this] def tableHeader(res: QueryResult) = {
    def str(name: String) = if (res.source.exists(_.sortable)) {
      if (res.source.flatMap(_.sortedColumn).contains(name)) {
        val asc = !res.source.flatMap(_.sortedAscending).contains(false)
        span(cls := "sorted-title", data("col") := name, data("dir") := { if (asc) { "asc" } else { "desc" } })(
          i(cls := s"right sorted-icon fa ${if (asc) { Icons.sortedAsc } else { Icons.sortedDesc }}"),
          span(name)
        )
      } else {
        span(cls := "sorted-title", data("col") := name, data("dir") := "none")(
          i(cls := s"right sort-icon fa ${Icons.sortable}"),
          span(name)
        )
      }
    } else {
      span(cls := "unsorted-title")(name)
    }
    thead(tr(res.columns.map(c => th(data("t") := c.t.toString)(str(c.name)))))
  }

  private[this] def cellValue(col: QueryResult.Col, v: Option[String]) = {
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
        case ByteArrayType => if (x.length > 200) {
          span(title := x.trim)(x.substring(0, 200) + "...")
        } else {
          span(x)
        }
        case _ => span(s"$x (${col.t})")
      }) -> true
      case None => span(title := "Null")("∅") -> false
      // scalastyle:off
      case null => span("null-bug") -> false
      // scalastyle:on
    }
    col.relationTable match {
      case Some(relTable) if contentEl._2 => td(
        a(
          cls := "query-rel-link",
          href := s"#table-$relTable::${col.relationColumn.getOrElse("")}=${v.getOrElse("")}",
          title := s"Open [$relTable] table filtered where [${col.relationColumn.getOrElse("")}=${v.getOrElse("0")}]",
          data("rel-table") := relTable,
          data("rel-col") := col.relationColumn.getOrElse(""),
          data("rel-val") := v.getOrElse("")
        )(i(cls := s"fa ${Icons.relation}")),
        span(contentEl._1)
      )
      case _ => td(contentEl._1)
    }
  }

  def tableRows(res: QueryResult) = {
    res.data.map(r => tr(res.columns.zip(r).map(x => cellValue(x._1, x._2))))
  }

  def forResults(res: QueryResult) = if (res.columns.isEmpty || res.data.isEmpty) {
    em("No rows returned.")
  } else {
    div(cls := "query-result-table")(
      table(cls := "bordered highlight responsive-table")(
        tableHeader(res),
        tbody(tableRows(res))
      )
    )
  }
}
