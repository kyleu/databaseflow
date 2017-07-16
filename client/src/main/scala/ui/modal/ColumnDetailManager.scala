package ui.modal

import models.GetColumnDetail
import models.schema.ColumnDetails
import models.template.column.ColumnTemplate
import org.scalajs.jquery.{JQuery, jQuery => $}
import util.{NetworkMessage, TemplateUtils}

import scala.scalajs.js

object ColumnDetailManager {
  private[this] val modal = js.Dynamic.global.$("#column-detail-modal")

  private[this] val modalContent = $("#column-detail-modal-content", modal)
  private[this] val modalLink = $("#column-detail-ok-link", modal)

  private[this] var activeRequest: Option[(String, String)] = None

  def init() = TemplateUtils.clickHandler(modalLink, _ => close())

  def installHandlers(selector: JQuery, owner: String) = {
    TemplateUtils.clickHandler(selector, jq => ColumnDetailManager.show(owner, jq.data("col").toString, jq.data("t").toString))
  }

  def show(owner: String, name: String, t: String) = {
    //util.Logging.info(s"Column details for $modelType [$modelName::$name]. ($t)")

    val content = ColumnTemplate.columnDetailsPanel(owner, name, t)
    modalContent.html(content.toString)
    modal.openModal()

    activeRequest = Some((owner, name))
    NetworkMessage.sendMessage(GetColumnDetail(owner, name, t))
  }

  def onDetails(owner: String, name: String, details: ColumnDetails) = {
    activeRequest match {
      case Some((o, n)) if o != owner || n != name => // No op
      case _ =>
        val detailsContent = $(".stats-panel", modalContent)
        if (detailsContent.length != 1) {
          throw new IllegalStateException(s"Found [${detailsContent.length}] column detail elements.")
        }
        val content = ColumnTemplate.columnDetails(details)
        detailsContent.html(content.toString)
    }
  }

  def close(): Unit = modal.closeModal()
}
