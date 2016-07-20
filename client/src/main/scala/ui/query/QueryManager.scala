package ui.query

import java.util.UUID

import models.SubmitQuery
import org.scalajs.jquery.{JQuery, jQuery => $}
import ui.{ProgressManager, TabManager}

object QueryManager {
  var activeQueries = Seq.empty[UUID]

  lazy val workspace = $("#workspace")

  def addQuery(queryId: UUID, title: String, queryPanel: JQuery, onChange: (String) => Unit): Unit = {
    val sqlEditor = SqlManager.newEditor(queryId, onChange)

    def wire(q: JQuery, action: String) = utils.JQueryUtils.clickHandler(q, (jq) => {
      val resultId = UUID.randomUUID
      ProgressManager.startProgress(queryId, resultId, title)
      val sql = SqlManager.getActiveSql(queryId)
      utils.NetworkMessage.sendMessage(SubmitQuery(queryId, sql, Some(action), resultId))
    })

    def updateName() = $(".run-query-link", queryPanel).text(SqlManager.getLinkTitle(queryId))

    wire($(".run-query-link", queryPanel), "run")
    wire($(".explain-query-link", queryPanel), "explain")
    wire($(".analyze-query-link", queryPanel), "analyze")

    sqlEditor.selection.moveCursorFileEnd()
    sqlEditor.selection.on("changeSelection", updateName _)
    sqlEditor.focus()

    activeQueries = activeQueries :+ queryId

    updateName()
    SqlManager.check(queryId, SqlManager.getSql(queryId))
  }

  def closeQuery(queryId: UUID): Unit = {
    if (activeQueries.size == 1) {
      AdHocQueryManager.addNewQuery()
    }

    SqlManager.closeQuery(queryId)
    $(s"#panel-$queryId").remove()
    TabManager.removeTab(queryId)

    val originalIndex = activeQueries.indexOf(queryId)
    activeQueries = activeQueries.filterNot(_ == queryId)

    val newId = activeQueries(if (originalIndex < 1) { 0 } else { originalIndex - 1 })
    TabManager.selectTab(newId)
  }
}
