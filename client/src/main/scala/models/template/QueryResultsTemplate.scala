package models.template

import java.util.UUID

import models.query.QueryResult

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryResultsTemplate {
  def forRowResults(qr: QueryResult, dateIsoString: String, durationMs: Int, resultId: UUID) = {
    val source = qr.source.getOrElse(throw new IllegalStateException(s"Missing source for row data, result [$resultId]"))
    val hiddenClass = source.sortedColumn match {
      case Some(_) => "initially-hidden"
      case None => ""
    }
    val content = div(id := resultId.toString)(
      div(cls := "row-status-display")(
        a(href := "#", cls := s"results-filter-link right theme-text $hiddenClass")("Filter"),
        p(
          s"${utils.NumberUtils.withCommas(qr.rowsAffected)} rows returned ",
          time(cls := "timeago", "datetime".attr := dateIsoString)(dateIsoString),
          s" in [${durationMs}ms]."
        ),
        div(source.filterColumn match {
          case Some(column) =>
            val op = source.filterOp.getOrElse("?")
            val v = source.filterValue.getOrElse("?")
            s"$column $op $v"
          case None => ""
        })
      ),

      DataFilterTemplate.forResults(qr, resultId),
      DataTableTemplate.forResults(qr, resultId),
      div(cls := "additional-results")(
        a(cls := "append-rows-link theme-text initially-hidden", data("offset") := "0", data("limit") := qr.data.size.toString, href := "#")(
          s"Load ${qr.data.size} More Rows"
        ),
        em(cls := "no-rows-remaining initially-hidden")("No more rows available")
      )
    )

    StaticPanelTemplate.cardRow(
      content = content,
      showClose = false
    )
  }

  def forQueryResults(qr: QueryResult, dateIsoString: String, durationMs: Int, resultId: UUID) = {
    val content = div(id := resultId.toString)(
      a(href := "#", cls := "results-sql-link right theme-text")("SQL"),
      p(
        s"${utils.NumberUtils.withCommas(qr.rowsAffected)} rows returned ",
        time(cls := "timeago", "datetime".attr := dateIsoString)(dateIsoString),
        s" in [${durationMs}ms]."
      ),
      div(cls := "z-depth-1 query-result-sql")(
        pre(cls := "pre-wrap")(qr.sql)
      ),

      DataTableTemplate.forResults(qr, resultId),

      div(cls := "additional-results")(
        a(cls := "append-rows-link theme-text initially-hidden", data("offset") := "0", data("limit") := qr.data.size.toString, href := "#")(
          s"Load ${qr.data.size} More Rows"
        ),
        em(cls := "no-rows-remaining initially-hidden")("No more rows available")
      )
    )

    StaticPanelTemplate.cardRow(
      content = content,
      showClose = false
    )
  }

  def forStatementResults(qr: QueryResult, dateIsoString: String, durationMs: Int) = {
    val content = div(
      p(s"${qr.rowsAffected} rows affected ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateIsoString), s" in [${durationMs}ms]."),
      div(cls := "z-depth-1 statement-result-sql")(
        pre(cls := "pre-wrap")(qr.sql)
      )
    )

    StaticPanelTemplate.cardRow(
      content = content,
      showClose = false
    )
  }

  def forAppend(qr: QueryResult, resultId: UUID) = {
    val addRowNum = !qr.columns.headOption.exists(_.name == "row_num")
    val rows = DataTableTemplate.tableRows(qr, resultId, addRowNum)
    rows.map(_.render).mkString("\n")
  }
}
