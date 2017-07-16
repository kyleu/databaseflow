package services.query

import java.util.UUID

import models.query.QueryResult
import models.query.QueryResult.Source
import models.template.query.QueryResultsTemplate
import org.scalajs.dom
import org.scalajs.jquery.{JQuery, jQuery => $}
import ui.query.{FilterManager, RowDataManager}
import ui.ProgressManager
import ui.tabs.TabManager
import util.{Config, Logging, TemplateUtils}

import scala.scalajs.js
import scala.util.Random

object QueryResultService {
  def handleNewQueryResults(resultId: UUID, index: Int, result: QueryResult, durationMs: Int): Unit = {
    val occurred = new scalajs.js.Date(result.occurred.toDouble)
    TransactionService.incrementCount()

    val chartId = UUID.randomUUID
    val key = Random.alphanumeric.take(6).mkString

    val content = QueryResultsTemplate.forQueryResults(result, occurred.toISOString, durationMs, key, resultId, chartId)
    ProgressManager.completeProgress(result.queryId, resultId, index, content)

    val panel = $(s"#$resultId", $(s"#workspace-${result.queryId}"))
    val resultEl = $("." + key, panel)
    QueryEventHandlers.wireResults(resultEl, result)

    result.source.foreach { src =>
      onComplete(result, src, panel, resultId)
      QueryEventHandlers.wireLinks(panel, result, chartId)
    }

    val sqlEl = $(".query-result-sql", panel)
    var sqlShown = false
    TemplateUtils.clickHandler($(".results-sql-link", panel), _ => {
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

    dom.document.getElementById(resultId.toString).scrollIntoView()
    dom.window.scrollBy(0, if (TabManager.tabCount == 1) { -60 } else { -110 })
  }

  private[this] def onComplete(result: QueryResult, src: Source, panel: JQuery, resultId: UUID) = {
    js.Dynamic.global.$(".filter-select", panel).material_select()

    FilterManager.init(src.t, result.queryId, src.name, panel, src, result.columns, resultId)

    val options = src.asRowDataOptions(Some(Config.pageSize))
    TemplateUtils.clickHandler($(".sorted-title", panel), (j) => {
      val col = j.data("col").toString
      val asc = j.data("dir").toString == "asc"
      val newOptions = options.copy(orderByCol = Some(col), orderByAsc = Some(!asc))
      RowDataManager.showRowData(src.t, result.queryId, src.name, newOptions, resultId)
    })

    TemplateUtils.clickHandler($(".filter-cancel-link", panel), _ => {
      val newOptions = options.copy(filters = Nil)
      RowDataManager.showRowData(src.t, result.queryId, src.name, newOptions, resultId)
    })

    val appendRowsLink = $(".append-rows-link", panel)
    TemplateUtils.clickHandler(appendRowsLink, _ => {
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
}
