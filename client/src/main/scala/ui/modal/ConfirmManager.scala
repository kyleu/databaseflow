package ui.modal

import org.scalajs.jquery.{jQuery => $}

import scala.scalajs.js

object ConfirmManager {
  private[this] var activeCallback: Option[(Boolean) => Unit] = None
  private[this] val modal = js.Dynamic.global.$("#confirm-modal")

  private[this] val confirmContent = $("#confirm-modal-content", modal)
  private[this] val linkTrue = $("#confirm-true-link", modal)
  private[this] val linkFalse = $("#confirm-false-link", modal)

  def init() = {
    utils.JQueryUtils.clickHandler($("#confirm-true-link", modal), (jq) => {
      activeCallback match {
        case Some(cb) => cb(true)
        case None => throw new IllegalStateException("No active callback.")
      }
    })

    utils.JQueryUtils.clickHandler($("#confirm-false-link", modal), (jq) => activeCallback match {
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

  def close(): Unit = modal.closeModal()
}
