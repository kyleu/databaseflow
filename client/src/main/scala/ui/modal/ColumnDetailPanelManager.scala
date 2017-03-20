package ui.modal

import models.template.column.ColumnTemplate
import org.scalajs.jquery.{jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

object ColumnDetailPanelManager {
  private[this] val modal = js.Dynamic.global.$("#column-detail-modal")

  private[this] val modalContent = $("#column-detail-modal-content", modal)
  private[this] val modalLink = $("#column-detail-ok-link", modal)

  def init() = TemplateUtils.clickHandler(modalLink, jq => {
    close()
  })

  def show(modelType: String, modelName: String, name: String, t: String) = {
    utils.Logging.info(s"Column details for $modelType [$modelName::$name]. ($t)")

    val content = ColumnTemplate.columnDetails(modelType, modelName, name, t)
    modalContent.html(content.toString)
    modal.openModal()
  }

  def close(): Unit = modal.closeModal()
}
