package models.template.query

import java.util.UUID

import models.query.QueryResult
import models.template.StaticPanelTemplate
import models.template.results.{ DataFilterTemplate, DataTableTemplate }

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryResultsTemplate {
  def forQueryResults(qr: QueryResult, dateIsoString: String, durationMs: Int, resultId: UUID) = {
    val hiddenClass = qr.source.flatMap(_.sortedColumn) match {
      case Some(_) => "initially-hidden"
      case None => ""
    }
    val content = div(id := resultId.toString)(
      div(cls := "row-status-display")(
        qr.source match {
          case Some(_) => a(href := "#", cls := s"results-filter-link right theme-text $hiddenClass")("Filter")
          case None => span()
        },
        a(href := "#", cls := "results-sql-link right theme-text")("SQL"),
        p(
          s"${utils.NumberUtils.withCommas(qr.rowsAffected)} ",
          span(cls := "total-row-count"),
          " rows returned ",
          time(cls := "timeago", "datetime".attr := dateIsoString)(dateIsoString),
          " in [",
          span(cls := "total-duration")(durationMs.toString),
          "ms]."
        ),
        div(qr.source.flatMap(_.filterColumn) match {
          case Some(column) =>
            val op = qr.source.flatMap(_.filterOp).getOrElse("?")
            val v = qr.source.flatMap(_.filterValue).getOrElse("?")
            s"$column $op $v"
          case None => ""
        })
      ),

      DataFilterTemplate.forResults(qr, resultId),

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
    val containsRowNum = qr.columns.headOption.exists(_.name == "#")
    val rows = DataTableTemplate.tableRows(qr, resultId, containsRowNum)
    rows.map(_.render).mkString("\n")
  }
}
