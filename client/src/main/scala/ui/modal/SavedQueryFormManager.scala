package ui.modal

import models.QuerySaveRequest
import models.query.SavedQuery
import org.scalajs.jquery.{jQuery => $}
import services.NavigationService
import ui.query.{QueryManager, SavedQueryManager}
import utils.{Logging, NetworkMessage, TemplateUtils}

import scala.scalajs.js

object SavedQueryFormManager {
  val modal = js.Dynamic.global.$("#save-query-modal")

  private[this] var activeQuery: Option[SavedQuery] = None
  private[this] val inputName = $("#input-query-name", modal)
  private[this] val inputDescription = $("#input-query-description", modal)
  private[this] val inputConnectionTrue = $("#input-query-connection-true", modal)
  private[this] val inputConnectionFalse = $("#input-query-connection-false", modal)

  def init() = {
    TemplateUtils.clickHandler($("#input-query-cancel-link", modal), jq => modal.closeModal())
    TemplateUtils.clickHandler($("#input-query-save-link", modal), jq => save())
  }

  def show(savedQuery: SavedQuery) = {
    inputName.value(savedQuery.name)
    inputDescription.value(savedQuery.description.getOrElse(""))

    savedQuery.read match {
      case "visitor" => $("#input-query-read-visitor", modal).prop("checked", true)
      case "user" => $("#input-query-read-user", modal).prop("checked", true)
      case "admin" => $("#input-query-read-admin", modal).prop("checked", true)
      case "private" => $("#input-query-read-private", modal).prop("checked", true)
      case x => throw new IllegalStateException(x)
    }
    savedQuery.edit match {
      case "visitor" => $("#input-query-edit-visitor", modal).prop("checked", true)
      case "user" => $("#input-query-edit-user", modal).prop("checked", true)
      case "admin" => $("#input-query-edit-admin", modal).prop("checked", true)
      case "private" => $("#input-query-edit-private", modal).prop("checked", true)
      case x => throw new IllegalStateException(x)
    }
    savedQuery.connection match {
      case Some(conn) => inputConnectionTrue.prop("checked", true)
      case None => inputConnectionFalse.prop("checked", true)
    }

    activeQuery = Some(savedQuery)
    modal.openModal()
    inputName.focus()
  }

  def handleSavedQuery(sq: SavedQuery, error: Option[String]) = {
    if (activeQuery.exists(_.id == sq.id)) {
      error match {
        case Some(err) => Logging.error("Cannot save query: " + err)
        case None =>
          SavedQueryManager.updateSavedQueries(Seq(sq), Map.empty)
          if (!SavedQueryManager.openSavedQueries.contains(sq.id)) {
            QueryManager.closeQuery(sq.id)
          }
          SavedQueryManager.savedQueryDetail(sq.id)
      }
      modal.closeModal()
    } else {
      SavedQueryManager.updateSavedQueries(Seq(sq), Map.empty)
    }
    val status = $(s"#panel-${sq.id} .unsaved-status")
    if (status.length != 1) {
      throw new IllegalStateException(s"Panel has [${status.length}] status elements.")
    }
    status.hide()
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
    val read = $("input[name=read]:checked", modal).value().toString
    val edit = $("input[name=edit]:checked", modal).value().toString
    val updated = activeQuery.getOrElse(throw new IllegalStateException()).copy(
      name = inputName.value().toString,
      description = desc,
      connection = conn,
      read = read,
      edit = edit
    )
    NetworkMessage.sendMessage(QuerySaveRequest(updated))
  }
}
