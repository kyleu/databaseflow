package services

import java.util.UUID

import models.query.QueryResult.Source
import models.query.{ QueryResult, RowDataOptions }
import models.schema.FilterOp
import models.template.query.QueryResultsTemplate
import org.scalajs.jquery.{ JQuery, jQuery => $ }
import ui.ProgressManager
import ui.query.{ RowDataManager, TableManager }
import utils.JQueryUtils

import scala.scalajs.js

object QueryResultService {
  def handleNewQueryResults(resultId: UUID, result: QueryResult, durationMs: Int): Unit = {
    val occurred = new scalajs.js.Date(result.occurred.toDouble)
    if (result.isStatement) {
      val content = QueryResultsTemplate.forStatementResults(result, occurred.toISOString, durationMs)
      ProgressManager.completeProgress(result.queryId, resultId, content)
    } else {
      val content = QueryResultsTemplate.forQueryResults(result, occurred.toISOString, durationMs, resultId)
      ProgressManager.completeProgress(result.queryId, resultId, content)

      val workspace = $(s"#workspace-${result.queryId}")
      val panel = $(s"#$resultId", workspace)
      val resultEl = $(".query-result-table", panel)

      JQueryUtils.clickHandler($(".query-rel-link", resultEl), (jq) => {
        val table = jq.data("rel-table").toString
        val col = jq.data("rel-col").toString
        val v = jq.data("rel-val").toString
        TableManager.tableDetail(table, RowDataOptions(filterCol = Some(col), filterOp = Some(FilterOp.Equal), filterVal = Some(v)))
      })

      result.source match {
        case Some(src) => onComplete(result, src, panel, resultId)
        case None => // No op
      }

      val sqlEl = $(".query-result-sql", panel)
      val sqlLink = $(".results-sql-link", panel)
      var sqlShown = false
      utils.JQueryUtils.clickHandler(sqlLink, (jq) => {
        if (sqlShown) { sqlEl.hide() } else { sqlEl.show() }
        sqlShown = !sqlShown
      })

      if (result.moreRowsAvailable) {
        $(".additional-results .append-rows-link").show()
        $(".additional-results .no-rows-remaining").hide()
      } else {
        $(".additional-results .append-rows-link").hide()
        $(".additional-results .no-rows-remaining").show()
      }
    }
  }

  private[this] def onComplete(result: QueryResult, src: Source, panel: JQuery, resultId: UUID) = {
    js.Dynamic.global.$(".filter-select", panel).material_select()

    utils.JQueryUtils.clickHandler($(".results-filter-link", panel), (jq) => {
      $(".row-status-display", panel).hide()
      $(".filter-container", panel).show()
    })
    utils.JQueryUtils.clickHandler($(".results-filter-cancel", panel), (jq) => {
      $(".row-status-display", panel).show()
      $(".filter-container", panel).hide()
    })

    val options = src.asRowDataOptions
    JQueryUtils.clickHandler($(".sorted-title", panel), (j) => {
      val col = j.data("col").toString
      val asc = j.data("dir").toString == "asc"
      val newOptions = options.copy(orderByCol = Some(col), orderByAsc = Some(!asc))
      RowDataManager.showRowData(src.t, result.queryId, src.name, newOptions)
    })

    val appendRowsLink = $(".append-rows-link", panel)
    JQueryUtils.clickHandler(appendRowsLink, (j) => {
      val limit = appendRowsLink.data("limit").toString.toInt
      val offset = appendRowsLink.data("offset").toString.toInt match {
        case 0 => 100
        case x => x
      }
      val newOptions = options.copy(limit = Some(limit), offset = Some(offset))
      utils.Logging.info(s"Requesting additional rows from offset [${newOptions.offset.getOrElse(0)}] and limit [$limit].")
      appendRowsLink.data("offset", offset + limit)
      appendRowsLink.hide()
      RowDataManager.showRowData(src.t, result.queryId, src.name, newOptions, resultId)
    })
  }
}
