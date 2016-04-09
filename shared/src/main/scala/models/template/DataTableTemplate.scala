package models.template

import models.query.QueryResult
import models.schema.ColumnType._

import scalatags.Text.all._

object DataTableTemplate {
  private[this] def tableHeader(res: QueryResult) = {

    def str(name: String) = if (res.sortable) {
      if (res.sortedColumn.contains(name)) {
        val icon = if (res.sortedAscending.contains(false)) { Icons.sortedDesc } else { Icons.sortedAsc }
        Seq(i(cls := s"right fa $icon"), span(cls := "sorted-title")(name))
      } else {
        Seq(i(cls := s"sort-icon fa ${Icons.sortable}"), span(cls := "sorted-title")(name))
      }
    } else {
      Seq(span(name))
    }
    thead(tr(res.columns.map(c => th(title := c.t.toString)(str(c.name): _*))))
  }

  private[this] def cellValue(col: QueryResult.Col, v: Option[String]) = {
    val contentEl = v match {
      case Some(x) => (col.t match {
        case StringType if x.isEmpty => em("empty string")
        case StringType => span(x)
        case IntegerType => span(x)
        case ShortType => span(x)
        case TimestampType => span(x)
        case BooleanType => span(x)
        case ByteArrayType => em("byte array")
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
          href := "#",
          title := s"Open [$relTable] table filtered with [${col.relationColumn.getOrElse("")} ${v.getOrElse("0")}]",
          data("rel-table") := relTable,
          data("rel-col") := col.relationColumn.getOrElse(""),
          data("rel-val") := v.getOrElse("")
        )(
            i(cls := s"fa ${Icons.relation}"),
            span(contentEl._1)
          )
      )
      case _ => td(contentEl._1)
    }
  }

  private[this] def tableBody(res: QueryResult) = {
    tbody(res.data.map(r => tr(res.columns.zip(r).map(x => cellValue(x._1, x._2)))))
  }

  def forResults(res: QueryResult) = {
    val data = if (res.columns.isEmpty || res.data.isEmpty) {
      em("No rows returned.")
    } else {
      div(cls := "query-result-table")(
        table(cls := "bordered highlight responsive-table")(
          tableHeader(res),
          tableBody(res)
        )
      )
    }

    data
  }
}
