package ui.modal

import org.scalajs.jquery.{jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

object ConfirmManager {
  private[this] var activeCallback: Option[(Boolean) => Unit] = None
  private[this] val modal = js.Dynamic.global.$("#confirm-modal")

  private[this] val confirmContent = $("#confirm-modal-content", modal)
  private[this] val linkTrue = $("#confirm-true-link", modal)
  private[this] val linkFalse = $("#confirm-false-link", modal)

  def init() = {
    TemplateUtils.clickHandler(linkTrue, _ => {
      activeCallback match {
        case Some(cb) => cb(true)
        case None => throw new IllegalStateException("No active callback.")
      }
    })

    TemplateUtils.clickHandler(linkFalse, _ => activeCallback match {
      case Some(cb) => cb(false)
      case None => throw new IllegalStateException("No active callback.")
    })
  }

  def show(callback: (Boolean) => Unit, content: String, trueButton: String = "OK", falseButton: String = "Cancel") = {
    activeCallback = Some(callback)
    confirmContent.text(content)
    linkTrue.text(trueButton)
    linkFalse.text(falseButton)
    modal.openModal()
  }

  def close(): Unit = {
    modal.closeModal()
    $(".lean-overlay").hide()
  }
}
