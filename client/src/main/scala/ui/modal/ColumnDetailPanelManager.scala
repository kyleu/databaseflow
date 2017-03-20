package ui.modal

import models.GetColumnDetail
import models.template.column.ColumnTemplate
import org.scalajs.jquery.{jQuery => $}
import utils.{NetworkMessage, TemplateUtils}

import scala.scalajs.js

object ColumnDetailPanelManager {
  private[this] val modal = js.Dynamic.global.$("#column-detail-modal")

  private[this] val modalContent = $("#column-detail-modal-content", modal)
  private[this] val modalLink = $("#column-detail-ok-link", modal)

  private[this] var activeRequest: Option[(String, String)] = None

  def init() = TemplateUtils.clickHandler(modalLink, jq => {
    close()
  })

  def show(owner: String, name: String, t: String) = {
    //utils.Logging.info(s"Column details for $modelType [$modelName::$name]. ($t)")

    val content = ColumnTemplate.columnDetails(owner, name, t)
    modalContent.html(content.toString)
    modal.openModal()

    activeRequest = Some((owner, name))
    NetworkMessage.sendMessage(GetColumnDetail(owner, name, t))
  }

  def close(): Unit = modal.closeModal()
}
