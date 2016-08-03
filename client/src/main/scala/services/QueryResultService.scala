package services

import java.util.UUID

import models.QueryResultRowCount
import models.query.QueryResult.Source
import models.query.{QueryResult, RowDataOptions}
import models.schema.FilterOp
import models.template.query.QueryResultsTemplate
import org.scalajs.jquery.{JQuery, jQuery => $}
import ui.ProgressManager
import ui.query.{FilterManager, RowDataManager, TableManager}
import utils.{Logging, NumberUtils, TemplateUtils}

import scala.scalajs.js

object QueryResultService {
  def handleNewQueryResults(resultId: UUID, result: QueryResult, durationMs: Int): Unit = {
    val occurred = new scalajs.js.Date(result.occurred.toDouble)
    if (result.isStatement) {
      val content = QueryResultsTemplate.forStatementResults(result, occurred.toISOString, durationMs, resultId)
      ProgressManager.completeProgress(result.queryId, resultId, content)

      val panel = $(s"#$resultId", $(s"#workspace-${result.queryId}"))
      val sqlEl = $(".statement-result-sql", panel)
      var sqlShown = false
      TemplateUtils.clickHandler($(".results-sql-link", panel), (jq) => {
        if (sqlShown) { sqlEl.hide() } else { sqlEl.show() }
        sqlShown = !sqlShown
      })
    } else {
      val content = QueryResultsTemplate.forQueryResults(result, occurred.toISOString, durationMs, resultId)
      ProgressManager.completeProgress(result.queryId, resultId, content)

      val panel = $(s"#$resultId", $(s"#workspace-${result.queryId}"))
      val resultEl = $(".query-result-table", panel)

      TemplateUtils.clickHandler($(".query-rel-link", resultEl), (jq) => {
        val table = jq.data("rel-table").toString
        val col = jq.data("rel-col").toString
        val v = jq.data("rel-val").toString
        TableManager.tableDetail(table, RowDataOptions(filterCol = Some(col), filterOp = Some(FilterOp.Equal), filterVal = Some(v)))
      })

      result.source.foreach(src => onComplete(result, src, panel, resultId))

      val sqlEl = $(".query-result-sql", panel)
      var sqlShown = false
      TemplateUtils.clickHandler($(".results-sql-link", panel), (jq) => {
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

    FilterManager.init(src.t, result.queryId, src.name, panel, src)

    val options = src.asRowDataOptions
    TemplateUtils.clickHandler($(".sorted-title", panel), (j) => {
      val col = j.data("col").toString
      val asc = j.data("dir").toString == "asc"
      val newOptions = options.copy(orderByCol = Some(col), orderByAsc = Some(!asc))
      RowDataManager.showRowData(src.t, result.queryId, src.name, newOptions)
    })

    TemplateUtils.clickHandler($(".filter-cancel-link", panel), (jq) => {
      val newOptions = options.copy(filterCol = None, filterOp = None, filterVal = None)
      RowDataManager.showRowData(src.t, result.queryId, src.name, newOptions)
    })

    val appendRowsLink = $(".append-rows-link", panel)
    TemplateUtils.clickHandler(appendRowsLink, (j) => {
      val limit = appendRowsLink.data("limit").toString.toInt
      val offset = appendRowsLink.data("offset").toString.toInt match {
        case 0 => 100
        case x => x
      }
      val newOptions = options.copy(limit = Some(limit), offset = Some(offset))
      Logging.debug(s"Requesting additional rows from offset [${newOptions.offset.getOrElse(0)}] and limit [$limit].")
      appendRowsLink.data("offset", offset + limit)
      appendRowsLink.hide()
      RowDataManager.showRowData(src.t, result.queryId, src.name, newOptions, resultId)
    })
  }

  def handleResultRowCount(qrrc: QueryResultRowCount) = {
    val panel = $(s"#${qrrc.resultId}", $(s"#workspace-${qrrc.queryId}"))
    val rowCountEl = $(".total-row-count", panel)
    if (qrrc.overflow) {
      rowCountEl.text(s" of at least ${NumberUtils.withCommas(qrrc.count)} ")
    } else if (qrrc.count > 100) {
      rowCountEl.text(s" of ${NumberUtils.withCommas(qrrc.count)} total ")
    }
    $(".total-duration", panel).text(NumberUtils.withCommas(qrrc.durationMs))
  }
}
