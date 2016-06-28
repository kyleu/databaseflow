package ui

import java.util.UUID

import models.{GetQueryHistory, RemoveAuditHistory}
import models.audit.AuditRecord
import models.template.{HistoryTemplate, Icons}
import org.scalajs.jquery.{jQuery => $}
import ui.query.QueryManager
import utils.{JQueryUtils, NetworkMessage}

import scalatags.Text.all._

object HistoryManager {
  private[this] val historyId = UUID.fromString("88888888-8888-8888-8888-888888888888")
  private[this] var isOpen = false

  def show() = {
    if (isOpen) {
      TabManager.selectTab(historyId)
    } else {
      val panelHtml = div(id := s"panel-$historyId", cls := "workspace-panel")(HistoryTemplate.panel)
      WorkspaceManager.append(panelHtml.toString)

      def close() = {
        isOpen = false
        QueryManager.closeQuery(historyId)
      }

      TabManager.addTab(historyId, "history", "Query History", Icons.history, close)
      QueryManager.activeQueries = QueryManager.activeQueries :+ historyId

      val queryPanel = $(s"#panel-$historyId")
      JQueryUtils.clickHandler($(".refresh-history-link", queryPanel), e => {
        refresh()
      })

      refresh()
      isOpen = true
    }
  }

  def handleAuditHistoryResponse(history: Seq[AuditRecord]) = {
    val queryPanel = $(s"#panel-$historyId")
    val content = $(".history-content", queryPanel)
    content.html(HistoryTemplate.content(history).toString)

    JQueryUtils.clickHandler($(".audit-remove", content), (e) => {
      val id = UUID.fromString(e.data("audit").toString)
      NetworkMessage.sendMessage(RemoveAuditHistory(id))
    })

    JQueryUtils.relativeTime()
  }

  def handleAuditHistoryRemoved(id: UUID): Unit = {
    val queryPanel = $(s"#panel-$historyId")
    val row = $(s"#history-$id", queryPanel)
    row.remove()

    val remaining = $(".history-table tr", queryPanel)
    if (remaining.length == 1) {
      val content = $(".history-content", queryPanel)
      content.html(HistoryTemplate.content(Nil).toString)
    }
  }

  def refresh() = NetworkMessage.sendMessage(GetQueryHistory())
}
