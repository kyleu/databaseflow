package ui.modal

import models.SharedResultSaveRequest
import models.query.SharedResult
import models.user.Permission
import org.scalajs.jquery.{jQuery => $}
import services.NotificationService
import ui.query.SharedResultManager
import utils.{NetworkMessage, TemplateUtils}

import scala.scalajs.js

object SharedResultFormManager {
  val modal = js.Dynamic.global.$("#shared-result-modal")
  private[this] var activeSharedResult: Option[SharedResult] = None

  private[this] val inputTitle = $("#input-shared-result-title", modal)
  private[this] val inputDescription = $("#input-shared-result-description", modal)

  def init() = {
    TemplateUtils.clickHandler($("#input-shared-result-cancel-link", modal), jq => modal.closeModal())
    TemplateUtils.clickHandler($("#input-shared-result-share-link", modal), jq => share())
  }

  def show(sharedResult: SharedResult) = {
    utils.Logging.info("CS: " + sharedResult)
    activeSharedResult = Some(sharedResult)
    inputTitle.value(sharedResult.title)
    inputDescription.value(sharedResult.description.getOrElse(""))
    sharedResult.viewableBy match {
      case Permission.Visitor => $("#input-shared-result-visitor", modal).prop("checked", true)
      case Permission.User => $("#input-shared-result-user", modal).prop("checked", true)
      case Permission.Administrator => $("#input-shared-result-admin", modal).prop("checked", true)
      case Permission.Private => $("#input-shared-result-private", modal).prop("checked", true)
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
      val share = Permission.withName($("input[name=share]:checked", modal).value().toString)

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
