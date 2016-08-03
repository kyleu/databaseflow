package models.template.query

import java.util.UUID

import models.query.QueryResult
import models.schema.FilterOp
import models.template.{Icons, StaticPanelTemplate}
import models.template.results.{DataFilterTemplate, DataTableTemplate}
import utils.{NumberUtils, TemplateUtils}

import scalatags.Text.all._

object QueryResultsTemplate {
  def forQueryResults(qr: QueryResult, dateIsoString: String, durationMs: Int, resultId: UUID) = {
    val hasFilter = !(qr.isStatement || qr.data.isEmpty || qr.source.isEmpty)

    val content = div(id := resultId.toString)(
      div(qr.source.flatMap(_.filterColumn) match {
        case Some(column) =>
          val op = qr.source.flatMap(_.filterOp).getOrElse(FilterOp.Equal).symbol
          val v = qr.source.flatMap(_.filterValue).getOrElse("?")
          div(cls := "active-filter z-depth-1")(
            div(cls := "filter-cancel-link")(i(cls := "theme-text fa " + Icons.close)),
            i(cls := "fa " + Icons.filter),
            "Active Filter: ",
            strong(column),
            s" $op ",
            strong(v)
          )
        case None => ""
      }),

      div(cls := "row-status-display")(
        if (hasFilter) {
          a(href := "#", cls := "results-filter-link right theme-text")("Filter")
        } else {
          span()
        },
        a(href := "#", cls := "results-sql-link right theme-text")("SQL"),
        p(
          s"${NumberUtils.withCommas(qr.rowsAffected)} ",
          span(cls := "total-row-count"),
          " rows returned ",
          TemplateUtils.toTimeago(dateIsoString),
          " in [",
          span(cls := "total-duration")(durationMs.toString),
          "ms]."
        )
      ),

      if (hasFilter) {
        DataFilterTemplate.forResults(qr, resultId)
      } else {
        span()
      },

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

  def forStatementResults(qr: QueryResult, dateIsoString: String, durationMs: Int, resultId: UUID) = {
    val content = div(id := resultId.toString)(
      a(href := "#", cls := "results-sql-link right theme-text")("SQL"),
      p(s"${qr.rowsAffected} rows affected ", TemplateUtils.toTimeago(dateIsoString), s" in [${durationMs}ms]."),
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
