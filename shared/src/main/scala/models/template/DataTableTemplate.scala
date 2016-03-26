package models.template

import models.query.QueryResult
import models.schema.ColumnType._

import scalatags.Text.all._

object DataTableTemplate {
  private[this] def tableHeader(res: QueryResult) = {
    def str(name: String) = if (res.sortable) {
      val icon = if (res.sortedColumn.contains(name)) {
        if (res.sortedAscending.contains(false)) {
          "sort-up"
        } else {
          "sort-down"
        }
      } else {
        "sort"
      }
      Seq(i(cls := s"sort-icon fa fa-$icon"), span(cls := "sorted-title")(name))
    } else {
      Seq(span(name))
    }
    thead(tr(res.columns.map(c => th(title := c.t.toString)(str(c.name): _*))))
  }

  private[this] def cellValue(col: QueryResult.Col, v: Option[String]) = v match {
    case Some(x) => col.t match {
      case StringType if x.isEmpty => td(em("empty string"))
      case StringType => td(x)
      case IntegerType => td(x)
      case TimestampType => td(x)
      case _ => td(s"$x (${col.t})")
    }
    case None => td(title := "Null")("∅")
    case null => td("null-bug")
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
