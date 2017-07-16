package ui

import java.util.UUID

import models.{GetQueryHistory, RemoveAuditHistory}
import models.audit.AuditRecord
import models.template.{HistoryTemplate, Icons}
import org.scalajs.dom
import org.scalajs.jquery.{jQuery => $}
import ui.query.QueryManager
import ui.tabs.TabManager
import util.{NetworkMessage, TemplateUtils}

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

      def close() = if (QueryManager.closeQuery(historyId)) {
        isOpen = false
      }

      TabManager.addTab(historyId, "history", "Query History", Icons.history, close _)
      QueryManager.activeQueries = QueryManager.activeQueries :+ historyId

      val queryPanel = $(s"#panel-$historyId")
      TemplateUtils.clickHandler($(".refresh-history-link", queryPanel), _ => {
        refresh()
      })

      val msg = "This will remove all activity history for this connection. Are you sure?"
      TemplateUtils.clickHandler($(".remove-all-history-link", queryPanel), _ => {
        if (dom.window.confirm(msg)) {
          NetworkMessage.sendMessage(RemoveAuditHistory(None))
        }
      })

      refresh()
      isOpen = true
    }
  }

  def handleAuditHistoryResponse(history: Seq[AuditRecord]) = {
    val queryPanel = $(s"#panel-$historyId")
    val content = $(".history-content", queryPanel)
    content.html(HistoryTemplate.content(history).toString)

    TemplateUtils.clickHandler($(".audit-remove", content), (e) => {
      val id = UUID.fromString(e.data("audit").toString)
      NetworkMessage.sendMessage(RemoveAuditHistory(Some(id)))
    })

    TemplateUtils.relativeTime()
  }

  def handleAuditHistoryRemoved(id: Option[UUID]): Unit = {
    val queryPanel = $(s"#panel-$historyId")

    val row = id match {
      case Some(auditId) => $(s"#history-$auditId", queryPanel)
      case None => $(".history-table tbody tr", queryPanel)
    }
    row.remove()

    val remaining = $(".history-table tbody tr", queryPanel)
    if (remaining.length == 0) {
      val content = $(".history-content", queryPanel)
      content.html(HistoryTemplate.content(Nil).toString)
    }
  }

  def refresh() = NetworkMessage.sendMessage(GetQueryHistory())
}
