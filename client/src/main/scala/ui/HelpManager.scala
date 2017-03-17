package ui

import java.util.UUID

import models.template.{HelpTemplate, Icons}
import org.scalajs.jquery.{jQuery => $}
import ui.query.QueryManager
import ui.tabs.TabManager
import utils._

import scala.util.Random
import scalatags.Text.all._

object HelpManager {
  private[this] val helpId = UUID.fromString("77777777-7777-7777-7777-777777777777")
  private[this] var isOpen = false
  private[this] var tipIdx = Random.nextInt(TipsAndTricks.values.length)

  def show() = {
    if (isOpen) {
      updateCounts()
      TabManager.selectTab(helpId)
    } else {
      val panelHtml = div(id := s"panel-$helpId", cls := "workspace-panel")(HelpTemplate.content)

      WorkspaceManager.append(panelHtml.toString)

      def close() = if (QueryManager.closeQuery(helpId)) {
        isOpen = false
      }

      TabManager.addTab(helpId, "help", "Help", Icons.help, close)
      QueryManager.activeQueries = QueryManager.activeQueries :+ helpId

      val queryPanel = $(s"#panel-$helpId")

      val tipContent = $("#tip-detail", queryPanel)
      if (tipContent.length != 1) {
        throw new IllegalStateException(s"Encountered [${tipContent.length}] tip contents.")
      }

      tipContent.text(Messages("help.tip." + TipsAndTricks.values(tipIdx).key, utils.Config.projectName))

      TemplateUtils.clickHandler($(".previous-tip-link", queryPanel), jq => {
        if (tipIdx == 0) { tipIdx = TipsAndTricks.values.length - 1 } else { tipIdx = tipIdx - 1 }
        tipContent.text(Messages("help.tip." + TipsAndTricks.values(tipIdx).key, utils.Config.projectName))
      })
      TemplateUtils.clickHandler($(".next-tip-link", queryPanel), jq => {
        if (tipIdx == TipsAndTricks.values.length - 1) { tipIdx = 0 } else { tipIdx = tipIdx + 1 }
        tipContent.text(Messages("help.tip." + TipsAndTricks.values(tipIdx).key, utils.Config.projectName))
      })

      updateCounts()

      isOpen = true
    }
  }

  private[this] def updateCounts() = {
    val queryPanel = $(s"#panel-$helpId")
    val latency = NumberUtils.withCommas(NetworkMessage.latencyMs.getOrElse(0))
    val sentCount = NumberUtils.withCommas(utils.NetworkMessage.sentMessageCount)
    val receivedCount = NumberUtils.withCommas(utils.NetworkMessage.receivedMessageCount)
    val msg = Messages("help.connection.status", latency, sentCount, receivedCount)
    $(".connection-status", queryPanel).text(msg)
  }
}
