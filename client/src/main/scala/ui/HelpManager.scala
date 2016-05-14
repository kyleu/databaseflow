package ui

import java.util.UUID

import models.template.Icons
import org.scalajs.jquery.{ jQuery => $ }

object HelpManager {
  private[this] val helpId = UUID.fromString("77777777-7777-7777-7777-777777777777")

  def init() = {
    val queryPanel = $(s"#panel-$helpId")
    utils.JQueryUtils.clickHandler($(s".${Icons.close}", queryPanel), (jq) => {
      close()
    })
  }

  private[this] def close() = {
    if (QueryManager.activeQueries.size == 0) {
      AdHocQueryManager.addNewQuery()
    }

    $(s"#panel-$helpId").hide()
    TabManager.removeTab(helpId)

  }

  def show() = {
    TabManager.addTab(helpId, "help", "Help", Icons.help)
  }
}
