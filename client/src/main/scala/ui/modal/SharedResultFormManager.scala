package ui.modal

import models.SharedResultSaveRequest
import models.query.SharedResult
import org.scalajs.jquery.{jQuery => $}
import services.NotificationService
import ui.query.SharedResultManager
import utils.{Logging, NetworkMessage, TemplateUtils}

import scala.scalajs.js

object SharedResultFormManager {
  val modal = js.Dynamic.global.$("#share-results-modal")
  private[this] var activeSharedResult: Option[SharedResult] = None

  private[this] val inputTitle = $("#input-share-results-title", modal)
  private[this] val inputDescription = $("#input-share-results-description", modal)

  def init() = {
    TemplateUtils.clickHandler($("#input-share-results-cancel-link", modal), (jq) => modal.closeModal())
    TemplateUtils.clickHandler($("#input-share-results-share-link", modal), (jq) => share())
  }

  def show(sharedResult: SharedResult) = {
    activeSharedResult = Some(sharedResult)
    inputTitle.value(sharedResult.title)
    inputDescription.value(sharedResult.description.getOrElse(""))
    sharedResult.viewableBy match {
      case "visitor" => $("#input-share-results-visitor", modal).prop("checked", true)
      case "user" => $("#input-share-results-user", modal).prop("checked", true)
      case "admin" => $("#input-share-results-admin", modal).prop("checked", true)
      case "private" => $("#input-share-results-private", modal).prop("checked", true)
      case x => throw new IllegalStateException(x)
    }

    modal.openModal()
    inputTitle.focus()
  }

  private[this] def share() = {
    val title = inputTitle.value().toString.trim()
    if (title.isEmpty) {
      $(".share-name-error", modal).show()
    } else {
      $(".share-name-error", modal).hide()
      val desc = inputDescription.value().toString.trim() match {
        case d if d.isEmpty => None
        case d => Some(d)
      }
      val share = $("input[name=share]:checked", modal).value().toString

      val result = activeSharedResult.getOrElse(throw new IllegalStateException()).copy(title = title, description = desc, viewableBy = share)
      NetworkMessage.sendMessage(SharedResultSaveRequest(result))
    }
  }

  def handleSharedResult(sr: SharedResult, error: Option[String]) = error match {
    case Some(err) => NotificationService.info("Cannot save shared result", err)
    case None =>
      SharedResultManager.updateSharedResults(Seq(sr), Map.empty)
      modal.closeModal()
      NotificationService.info("Shared Result Saved", "Head to the Shared Results listing to see your new thing!")
  }
}
