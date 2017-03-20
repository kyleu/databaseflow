package ui.query

import org.scalajs.jquery.JQuery
import ui.modal.ColumnDetailPanelManager
import utils.TemplateUtils

object ColumnDetailManager {
  def installHandlers(selector: JQuery, modelType: String, modelName: String) = {
    TemplateUtils.clickHandler(selector, jq => onClick(modelType, modelName, jq.data("col").toString, jq.data("t").toString))
  }

  def onClick(modelType: String, modelName: String, name: String, t: String) = {
    ColumnDetailPanelManager.show(modelType, modelName, name, t)
  }
}
