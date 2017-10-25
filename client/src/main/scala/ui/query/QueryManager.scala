package ui.query

import java.util.UUID

import models.SubmitQuery
import models.query.SavedQuery
import org.scalajs.jquery.{JQuery, jQuery => $}
import services.TextChangeService
import services.query.ChartService
import ui.ProgressManager
import ui.tabs.TabManager
import util.{NetworkMessage, TemplateUtils}

object QueryManager {
  var activeQueries = Seq.empty[UUID]

  lazy val workspace = $("#workspace")

  def addQuery(queryId: UUID, title: String, queryPanel: JQuery, sql: String, params: Seq[SavedQuery.Param], onChange: (String) => Unit): Unit = {
    val sqlEditor = SqlManager.newEditor(queryId, onChange)

    def wire(q: JQuery, action: String, sql: () => (String, Seq[SavedQuery.Param])) = TemplateUtils.clickHandler(q, _ => {
      val r = sql()
      if (r._1.trim.nonEmpty) {
        val resultId = UUID.randomUUID
        ProgressManager.startProgress(queryId, resultId, title)
        NetworkMessage.sendMessage(SubmitQuery(queryId, r._1, r._2, Some(action), resultId))
      }
    })

    val runQueryLink = $(".run-query-link", queryPanel)
    wire(runQueryLink, "run", () => ParameterManager.getParams(SqlManager.getActiveSql(queryId), queryId))

    wire($(".explain-query-link", queryPanel), "explain", () => ParameterManager.getParams(SqlManager.getActiveSql(queryId), queryId))
    wire($(".analyze-query-link", queryPanel), "analyze", () => ParameterManager.getParams(SqlManager.getActiveSql(queryId), queryId))

    val runQueryAllLink = $(".run-query-all-link", queryPanel)
    wire(runQueryAllLink, "run", () => ParameterManager.getParams(SqlManager.getSql(queryId), queryId))

    val runSelectionLink = $(".run-query-selection-link", queryPanel)
    wire(runSelectionLink, "run", () => ParameterManager.getParams(sqlEditor.getSelectedText().toString.trim, queryId))

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
    ParameterManager.setValues(queryId, params)
    ParameterManager.onChange(queryId, sql, forceRefresh = true)
    val merged = ParameterManager.merge(sql, params)
    QueryCheckManager.check(queryId, merged)
  }

  def closeQuery(queryId: UUID): Boolean = if (TextChangeService.shouldClose(queryId)) {
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
    true
  } else {
    false
  }
}
