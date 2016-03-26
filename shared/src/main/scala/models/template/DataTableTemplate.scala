package models.template

import models.query.QueryResult
import models.schema.ColumnType._

import scalatags.Text.all._

object DataTableTemplate {
  private[this] def tableHeader(columns: Seq[QueryResult.Col]) = thead(tr(columns.map(c => th(title := c.t.toString)(c.name))))

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

  private[this] def tableBody(columns: Seq[QueryResult.Col], rows: Seq[Seq[Option[String]]]) = {
    tbody(rows.map(r => tr(columns.zip(r).map(x => cellValue(x._1, x._2)))))
  }

  def forResults(columns: Seq[QueryResult.Col], rows: Seq[Seq[Option[String]]]) = {
    val data = if (columns.isEmpty || rows.isEmpty) {
      em("No rows returned.")
    } else {
      div(cls := "query-result-table")(
        table(cls := "bordered highlight responsive-table")(
          tableHeader(columns),
          tableBody(columns, rows)
        )
      )
    }

    data
  }
}
