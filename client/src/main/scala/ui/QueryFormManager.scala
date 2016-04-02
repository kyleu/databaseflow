package ui

import models.{ QuerySaveRequest, QuerySaveResponse }
import models.query.SavedQuery
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import services.NavigationService
import utils.NetworkMessage

import scala.scalajs.js

object QueryFormManager {
  private[this] var activeQuery: Option[SavedQuery] = None
  private[this] val modal = js.Dynamic.global.$("#save-query-modal")
  private[this] val form = $("form", modal)

  private[this] val inputName = $("#input-query-name", modal)
  private[this] val inputDescription = $("#input-query-description", modal)
  private[this] val inputPublicTrue = $("#input-query-public-true", modal)
  private[this] val inputPublicFalse = $("#input-query-public-false", modal)
  private[this] val inputConnectionTrue = $("#input-query-connection-true", modal)
  private[this] val inputConnectionFalse = $("#input-query-connection-false", modal)

  def init() = {
    $("#input-query-cancel-link", modal).click { (e: JQueryEventObject) =>
      modal.closeModal()
      false
    }
    $("#input-query-save-link", modal).click { (e: JQueryEventObject) =>
      save()
      false
    }
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

  def handleResponse(sq: SavedQuery, error: Option[String]) = {
    if (activeQuery.exists(_.id == sq.id)) {
      utils.Logging.info(s"Received save response with ${error.map("error [" + _ + "]").getOrElse("no error")} for query [$sq].")
    } else {
      utils.Logging.warn(s"Received unhandled save response for unknown query [$sq].")
    }
  }

  private[this] def save() = {
    val updated = activeQuery.getOrElse(throw new IllegalStateException()).copy(
      name = inputName.value().toString,
      description = inputDescription.value().trim().toString match {
      case d if d.isEmpty => None
      case d => Some(d)
    },
      connection = if (inputConnectionTrue.is(":checked")) {
      None
    } else {
      Some(NavigationService.connectionId)
    },
      public = inputPublicTrue.is(":checked")
    )

    NetworkMessage.sendMessage(QuerySaveRequest(updated))
  }
}
