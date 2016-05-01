package models.template

import java.util.UUID

import models.query.QueryResult

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryResultsTemplate {
  def forQueryResults(qr: QueryResult, dateIsoString: String, dateFullString: String, durationMs: Int, resultId: UUID) = {
    val content = div(id := resultId.toString)(
      a(href := "#", cls := "results-sql-link right theme-text")("SQL"),

      p(
        s"${utils.NumberUtils.withCommas(qr.rowsAffected)} rows returned ",
        time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString),
        s" in [${durationMs}ms]."
      ),

      div(cls := "z-depth-1 query-result-sql")(
        pre(cls := "pre-wrap")(qr.sql)
      ),

      DataTableTemplate.forResults(qr),

      div(cls := "additional-results")(
        a(cls := "append-rows-link theme-text initially-hidden", data("offset") := "0", href := "#")(s"Load ${qr.data.size} More Rows"),
        em(cls := "no-rows-remaining initially-hidden")("No more rows available")
      )
    )

    StaticPanelTemplate.cardRow(
      content = content,
      showClose = false
    )
  }

  def forStatementResults(qr: QueryResult, dateIsoString: String, dateFullString: String, durationMs: Int) = {
    val content = div(
      p(s"${qr.rowsAffected} rows affected ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${durationMs}ms]."),
      div(cls := "z-depth-1 statement-result-sql")(
        pre(cls := "pre-wrap")(qr.sql)
      )
    )

    StaticPanelTemplate.cardRow(
      content = content,
      showClose = false
    )
  }

  def forAppend(qr: QueryResult) = {
    val rows = DataTableTemplate.tableRows(qr)
    rows.map(_.render).mkString("\n")
  }
}
