package models.template

import models.query.QueryResult

import scalatags.Text.all._

object DataTableTemplate {
  private[this] def tableHeader(columns: Seq[QueryResult.Col]) = thead(tr(columns.map(c => th(title := c.t)(c.name))))

  private[this] def cellValue(col: QueryResult.Col, v: Option[Any]) = v match {
    case Some(str: String) if str.isEmpty => td(em("empty string"))
    case Some(str: String) => td(str)
    case Some(x) => td(s"$x (${x.getClass.getName})")
    case None => td(title := "Null")("∅")
    case null => td("null-bug")
  }

  private[this] def tableBody(columns: Seq[QueryResult.Col], rows: Seq[Seq[Option[Any]]]) = {
    tbody(rows.map(r => tr(columns.zip(r).map(x => cellValue(x._1, x._2)))))
  }

  def forResults(columns: Seq[QueryResult.Col], rows: Seq[Seq[Option[Any]]]) = {
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
