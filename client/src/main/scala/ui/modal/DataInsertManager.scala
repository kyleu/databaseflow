package ui.modal

import org.scalajs.jquery.{jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

object DataInsertManager {
  private[this] val modal = js.Dynamic.global.$("#data-insert-modal")

  private[this] val confirmContent = $("#data-insert-modal-content", modal)
  private[this] val linkAdd = $("#data-insert-save-link", modal)

  def init() = {
    TemplateUtils.clickHandler(linkAdd, jq => {
      utils.Logging.info("Add link clicked...")
    })
  }

  def show() = {
    modal.openModal()
  }

  def close() = {
    modal.closeModal()
  }
}
