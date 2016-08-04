package ui

import java.util.UUID

import models.template.{HelpTemplate, Icons}
import org.scalajs.jquery.{jQuery => $}
import ui.query.QueryManager
import utils.{NetworkMessage, NumberUtils, TemplateUtils, TipsAndTricks}

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

      def close() = {
        isOpen = false
        QueryManager.closeQuery(helpId)
      }

      TabManager.addTab(helpId, "help", "Help", Icons.help, close)
      QueryManager.activeQueries = QueryManager.activeQueries :+ helpId

      val queryPanel = $(s"#panel-$helpId")

      val tipContent = $("#tip-detail", queryPanel)
      if (tipContent.length != 1) {
        throw new IllegalStateException(s"Encountered [${tipContent.length}] tip contents.")
      }

      tipContent.text(TipsAndTricks.values(tipIdx).content)

      TemplateUtils.clickHandler($(".previous-tip-link", queryPanel), jq => {
        if (tipIdx == 0) { tipIdx = TipsAndTricks.values.length - 1 } else { tipIdx = tipIdx - 1 }
        tipContent.text(TipsAndTricks.values(tipIdx).content)
      })
      TemplateUtils.clickHandler($(".next-tip-link", queryPanel), jq => {
        if (tipIdx == TipsAndTricks.values.length - 1) { tipIdx = 0 } else { tipIdx = tipIdx + 1 }
        tipContent.text(TipsAndTricks.values(tipIdx).content)
      })

      updateCounts()

      isOpen = true
    }
  }

  private[this] def updateCounts() = {
    val queryPanel = $(s"#panel-$helpId")

    val latency = NetworkMessage.latencyMs.getOrElse(0)
    $(".help-latency-label", queryPanel).text(NumberUtils.withCommas(latency))

    val sentCount = utils.NetworkMessage.sentMessageCount
    $(".help-sent-label", queryPanel).text(NumberUtils.withCommas(sentCount))

    val receivedCount = utils.NetworkMessage.receivedMessageCount
    $(".help-received-label", queryPanel).text(NumberUtils.withCommas(receivedCount))
  }
}
