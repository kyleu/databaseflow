package ui.modal

import java.util.UUID

import org.scalajs.jquery.{jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

object ShareResultsFormManager {
  private[this] val modal = js.Dynamic.global.$("#share-results-modal")

  private[this] val inputName = $("#input-share-results-name", modal)
  private[this] val inputDescription = $("#input-share-results-description", modal)

  def init() = {
    TemplateUtils.clickHandler($("#input-share-results-cancel-link", modal), (jq) => modal.closeModal())
    TemplateUtils.clickHandler($("#input-share-results-share-link", modal), (jq) => share())
  }

  def show(resultId: UUID, name: String) = {
    inputName.value(name)
    inputDescription.value("")

    modal.openModal()
    inputName.focus()
  }

  private[this] def share() = {
    val name = inputName.value().toString.trim()
    val desc = inputDescription.value().toString.trim() match {
      case d if d.isEmpty => None
      case d => Some(d)
    }
    val share = $("input[name=share]:checked", modal).value().toString
    utils.Logging.info(s"Share: $name ($desc): $share")
  }
}
