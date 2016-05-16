package services

import java.util.UUID

import models.query.{ QueryResult, RowDataOptions }
import models.schema.FilterOp
import models.template.QueryResultsTemplate
import org.scalajs.jquery.{ jQuery => $ }
import ui.{ ProgressManager, TableManager }
import utils.JQueryUtils

import scala.scalajs.js

object QueryResultService {
  def handleNewQueryResults(resultId: UUID, results: Seq[QueryResult], durationMs: Int): Unit = {
    val qr = results.head // TODO

    val occurred = new scalajs.js.Date(qr.occurred.toDouble)

    if (qr.isStatement) {
      val content = QueryResultsTemplate.forStatementResults(qr, occurred.toISOString, durationMs)
      ProgressManager.completeProgress(qr.queryId, resultId, content)
    } else {
      if (qr.source.isDefined) {
        val content = QueryResultsTemplate.forRowResults(qr, occurred.toISOString, durationMs, resultId)
        ProgressManager.completeProgress(qr.queryId, resultId, content)
      } else {
        val content = QueryResultsTemplate.forQueryResults(qr, occurred.toISOString, durationMs, resultId)
        ProgressManager.completeProgress(qr.queryId, resultId, content)
      }

      val workspace = $(s"#workspace-${qr.queryId}")
      val panel = $(s"#$resultId", workspace)

      val resultEl = $(".query-result-table", panel)

      JQueryUtils.clickHandler($(".query-rel-link", resultEl), (jq) => {
        val table = jq.data("rel-table").toString
        val col = jq.data("rel-col").toString
        val v = jq.data("rel-val").toString
        TableManager.tableDetail(table, RowDataOptions(filterCol = Some(col), filterOp = Some(FilterOp.Equal), filterVal = Some(v)))
      })

      if (qr.source.isDefined) {
        js.Dynamic.global.$(".filter-select", panel).material_select()

        val filterStatusEl = $(".row-status-display", panel)
        val filterEl = $(".filter-container", panel)

        utils.JQueryUtils.clickHandler($(".results-filter-link", panel), (jq) => {
          filterStatusEl.hide()
          filterEl.show()
        })
        utils.JQueryUtils.clickHandler($(".results-filter-cancel", panel), (jq) => {
          filterStatusEl.show()
          filterEl.hide()
        })
      } else {
        val sqlEl = $(".query-result-sql", panel)
        val sqlLink = $(".results-sql-link", panel)
        var sqlShown = false
        utils.JQueryUtils.clickHandler(sqlLink, (jq) => {
          if (sqlShown) { sqlEl.hide() } else { sqlEl.show() }
          sqlShown = !sqlShown
        })
      }

      if (qr.moreRowsAvailable) {
        $(".additional-results .append-rows-link").show()
        $(".additional-results .no-rows-remaining").hide()
      } else {
        $(".additional-results .append-rows-link").hide()
        $(".additional-results .no-rows-remaining").show()
      }
    }
  }
}
