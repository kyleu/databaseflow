package ui.query

import org.scalajs.jquery.JQuery
import ui.modal.ColumnDetailPanelManager
import utils.TemplateUtils

object ColumnDetailManager {
  def installHandlers(selector: JQuery, owner: String) = {
    TemplateUtils.clickHandler(selector, jq => onClick(owner, jq.data("col").toString, jq.data("t").toString))
  }

  def onClick(owner: String, name: String, t: String) = {
    ColumnDetailPanelManager.show(owner, name, t)
  }
}
