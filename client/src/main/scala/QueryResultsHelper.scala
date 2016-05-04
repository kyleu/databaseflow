import java.util.UUID

import models._
import models.query.{ QueryResult, RowDataOptions }
import models.template._
import org.scalajs.jquery.{ jQuery => $ }
import ui.{ ProgressManager, TableManager }
import utils.JQueryUtils

import scala.scalajs.js

trait QueryResultsHelper { this: DatabaseFlow =>
  protected[this] def handleQueryResultResponse(qrr: QueryResultResponse) = {
    if (qrr.results.size == 1 && qrr.results.headOption.exists(_.source.exists(_.dataOffset > 0))) {
      val result = qrr.results.headOption.getOrElse(throw new IllegalStateException())
      handleAppendQueryResult(qrr.id, result, qrr.durationMs)
    } else {
      handleNewQueryResults(qrr.id, qrr.results, qrr.durationMs)
    }
  }

  private[this] def handleNewQueryResults(resultId: UUID, results: Seq[QueryResult], durationMs: Int): Unit = {
    val qr = results.head // TODO

    val occurred = new scalajs.js.Date(qr.occurred.toDouble)

    if (qr.isStatement) {
      val content = QueryResultsTemplate.forStatementResults(qr, occurred.toISOString, occurred.toString, durationMs)
      ProgressManager.completeProgress(qr.queryId, resultId, content)
    } else {
      if (qr.source.isDefined) {
        val content = QueryResultsTemplate.forRowResults(qr, occurred.toISOString, occurred.toString, durationMs, resultId)
        ProgressManager.completeProgress(qr.queryId, resultId, content)
      } else {
        val content = QueryResultsTemplate.forQueryResults(qr, occurred.toISOString, occurred.toString, durationMs, resultId)
        ProgressManager.completeProgress(qr.queryId, resultId, content)
      }

      val workspace = $(s"#workspace-${qr.queryId}")
      val panel = $(s"#$resultId", workspace)

      val resultEl = $(".query-result-table", panel)

      JQueryUtils.clickHandler($(".query-rel-link", resultEl), (jq) => {
        val table = jq.data("rel-table").toString
        val col = jq.data("rel-col").toString
        val v = jq.data("rel-val").toString
        TableManager.tableDetail(table, RowDataOptions(filterCol = Some(col), filterOp = Some("="), filterVal = Some(v)))
      })

      if (qr.source.isDefined) {
        js.Dynamic.global.$(".filter-select", panel).material_select()

        val filterEl = $(".filter-container", panel)
        val filterLink = $(s".results-filter-link", panel)
        val filterCancelLink = $(s".results-filter-cancel", panel)

        utils.JQueryUtils.clickHandler(filterLink, (jq) => {
          filterLink.hide()
          filterEl.show()
        })
        utils.JQueryUtils.clickHandler(filterCancelLink, (jq) => {
          filterLink.show()
          filterEl.hide()
        })
      } else {
        val sqlEl = $(".query-result-sql", panel)
        val sqlLink = $(s".results-sql-link", panel)
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

  private[this] def handleAppendQueryResult(resultId: UUID, qr: QueryResult, durationMs: Int): Unit = {
    val workspace = $(s"#workspace-${qr.queryId}")
    val panel = $(s"#$resultId", workspace)
    val resultEl = $(".query-result-table tbody", panel)

    val content = QueryResultsTemplate.forAppend(qr)
    resultEl.append(content)

    JQueryUtils.clickHandler($(".query-rel-link", panel), (jq) => {
      val table = jq.data("rel-table").toString
      val col = jq.data("rel-col").toString
      val v = jq.data("rel-val").toString
      TableManager.tableDetail(table, RowDataOptions(filterCol = Some(col), filterOp = Some("="), filterVal = Some(v)))
    })

    if (qr.moreRowsAvailable) {
      $(".additional-results .append-rows-link").show()
      $(".additional-results .no-rows-remaining").hide()
    } else {
      $(".additional-results .append-rows-link").hide()
      $(".additional-results .no-rows-remaining").show()
    }

    utils.Logging.info(s"Appended [${qr.data.length}] rows to result [$resultId].")
  }

  protected[this] def handleQueryErrorResponse(qer: QueryErrorResponse): Unit = {
    val occurred = new scalajs.js.Date(qer.error.occurred.toDouble)
    val content = ErrorTemplate.forQueryError(qer, occurred.toISOString, occurred.toString)
    ProgressManager.completeProgress(qer.error.queryId, qer.id, content)
  }
}
