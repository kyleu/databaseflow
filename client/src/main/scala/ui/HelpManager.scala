package ui

import java.util.UUID

import models.template.{ HelpTemplate, Icons }
import org.scalajs.jquery.{ jQuery => $ }
import ui.query.QueryManager

import scalatags.Text.all._

object HelpManager {
  private[this] val helpId = UUID.fromString("77777777-7777-7777-7777-777777777777")
  private[this] var isOpen = false

  def show() = {
    if (isOpen) {
      TabManager.selectTab(helpId)
    } else {
      val template = HelpTemplate.content()
      val panelHtml = div(id := s"panel-$helpId", cls := "workspace-panel")(template)

      WorkspaceManager.append(panelHtml.toString)
      TabManager.addTab(helpId, "help", "Help", Icons.help)
      QueryManager.activeQueries = QueryManager.activeQueries :+ helpId

      val queryPanel = $(s"#panel-$helpId")

      utils.JQueryUtils.clickHandler($(s".${Icons.close}", queryPanel), (jq) => {
        isOpen = false
        QueryManager.closeQuery(helpId)
      })

      isOpen = true
    }
  }
}
