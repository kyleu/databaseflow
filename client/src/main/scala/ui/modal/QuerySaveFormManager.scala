package ui.modal

import models.QuerySaveRequest
import models.query.SavedQuery
import org.scalajs.jquery.{ jQuery => $ }
import services.NavigationService
import ui.query.SavedQueryManager
import utils.NetworkMessage

import scala.scalajs.js

object QuerySaveFormManager {
  private[this] var activeQuery: Option[SavedQuery] = None
  private[this] val modal = js.Dynamic.global.$("#save-query-modal")

  private[this] val inputName = $("#input-query-name", modal)
  private[this] val inputDescription = $("#input-query-description", modal)
  private[this] val inputPublicTrue = $("#input-query-public-true", modal)
  private[this] val inputPublicFalse = $("#input-query-public-false", modal)
  private[this] val inputConnectionTrue = $("#input-query-connection-true", modal)
  private[this] val inputConnectionFalse = $("#input-query-connection-false", modal)

  def init() = {
    utils.JQueryUtils.clickHandler($("#input-query-cancel-link", modal), (jq) => modal.closeModal())
    utils.JQueryUtils.clickHandler($("#input-query-save-link", modal), (jq) => save())
  }

  def show(savedQuery: SavedQuery) = {
    inputName.value(savedQuery.name)
    inputDescription.value(savedQuery.description.getOrElse(""))

    if (savedQuery.public) {
      inputPublicTrue.prop("checked", true)
    } else {
      inputPublicFalse.prop("checked", true)
    }
    savedQuery.connection match {
      case Some(conn) => inputConnectionTrue.prop("checked", true)
      case None => inputConnectionFalse.prop("checked", true)
    }

    activeQuery = Some(savedQuery)

    modal.openModal()
    inputName.focus()
  }

  def handleQuerySaveResponse(sq: SavedQuery, error: Option[String]) = {
    if (activeQuery.exists(_.id == sq.id)) {
      error match {
        case Some(err) => utils.Logging.error("Cannot save query: " + err)
        case None => SavedQueryManager.updateSavedQueries(Seq(sq))
      }
      modal.closeModal()
    } else {
      utils.Logging.warn(s"Received unhandled save response for unknown query [$sq].")
    }
  }

  private[this] def save() = {
    val desc = inputDescription.value().trim().toString match {
      case d if d.isEmpty => None
      case d => Some(d)
    }
    val conn = if (inputConnectionTrue.is(":checked")) {
      None
    } else {
      Some(NavigationService.connectionId)
    }
    val updated = activeQuery.getOrElse(throw new IllegalStateException()).copy(
      name = inputName.value().toString,
      description = desc,
      connection = conn,
      public = inputPublicTrue.is(":checked")
    )
    NetworkMessage.sendMessage(QuerySaveRequest(updated))
  }
}
