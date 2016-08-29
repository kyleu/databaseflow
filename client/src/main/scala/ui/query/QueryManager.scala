package ui.query

import java.util.UUID

import models.SubmitQuery
import org.scalajs.jquery.{JQuery, jQuery => $}
import services.ChartService
import ui.{ProgressManager, TabManager}
import utils.{NetworkMessage, TemplateUtils}

object QueryManager {
  var activeQueries = Seq.empty[UUID]

  lazy val workspace = $("#workspace")

  def addQuery(queryId: UUID, title: String, queryPanel: JQuery, onChange: (String) => Unit): Unit = {
    val sqlEditor = SqlManager.newEditor(queryId, onChange)

    def wire(q: JQuery, action: String, sql: () => String) = TemplateUtils.clickHandler(q, jq => {
      val resultId = UUID.randomUUID
      ProgressManager.startProgress(queryId, resultId, title)
      NetworkMessage.sendMessage(SubmitQuery(queryId, sql(), Some(action), resultId))
    })

    val runQueryLink = $(".run-query-link", queryPanel)
    wire(runQueryLink, "run", () => SqlManager.getActiveSql(queryId))

    wire($(".explain-query-link", queryPanel), "explain", () => SqlManager.getActiveSql(queryId))
    wire($(".analyze-query-link", queryPanel), "analyze", () => SqlManager.getActiveSql(queryId))

    val runQueryAllLink = $(".run-query-all-link", queryPanel)
    wire(runQueryAllLink, "run", () => SqlManager.getSql(queryId))

    val runSelectionLink = $(".run-query-selection-link", queryPanel)
    wire(runSelectionLink, "run", () => sqlEditor.getSelectedText().toString.trim)

    def showRunSelection() = {
      val txt = sqlEditor.getSelectedText().toString.trim
      if (txt.isEmpty) {
        runQueryLink.css("display", "inline")
        runSelectionLink.css("display", "none")
      } else {
        runQueryLink.css("display", "none")
        runSelectionLink.css("display", "inline")
      }
    }

    sqlEditor.selection.moveCursorFileEnd()
    sqlEditor.selection.on("changeSelection", showRunSelection _)
    sqlEditor.focus()

    activeQueries = activeQueries :+ queryId

    showRunSelection()
    SqlManager.updateLinks(queryId, runQueryLink, runQueryAllLink)
    SqlManager.check(queryId)
  }

  def closeQuery(queryId: UUID): Unit = {
    SqlManager.closeQuery(queryId)
    $(s"#panel-$queryId").remove()

    val originalIndex = activeQueries.indexOf(queryId)
    activeQueries = activeQueries.filterNot(_ == queryId)

    TabManager.removeTab(queryId)
    ChartService.closeCharts(queryId)

    if (activeQueries.nonEmpty) {
      val newId = activeQueries(if (originalIndex < 1) { 0 } else { originalIndex - 1 })
      TabManager.selectTab(newId)
    }
  }
}
