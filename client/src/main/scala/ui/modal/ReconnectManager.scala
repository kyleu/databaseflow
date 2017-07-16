package ui.modal

import org.scalajs.jquery.{jQuery => $}
import util.TemplateUtils

import scala.scalajs.js

object ReconnectManager {
  private[this] var activeCallback: Option[() => Unit] = None
  private[this] val modal = js.Dynamic.global.$("#reconnect-modal")

  private[this] val errorContent = $("#reconnect-error-content", modal)
  private[this] val link = $("#reconnect-action-link", modal)

  if (link.length != 1) {
    throw new IllegalStateException("Missing reconnect link.")
  }

  def init() = TemplateUtils.clickHandler(link, _ => {
    activeCallback match {
      case Some(cb) => cb()
      case None => throw new IllegalStateException("No active callback.")
    }
    close()
  })

  def show(callback: () => Unit, error: String) = {
    activeCallback = Some(callback)
    errorContent.text(error)
    modal.openModal()
  }

  def close(): Unit = modal.closeModal()
}
