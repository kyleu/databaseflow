package ui

import models.query.SavedQuery
import org.scalajs.jquery.{ jQuery => $ }

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
    $("", modal)
  }

  def show(savedQuery: SavedQuery) = {
    inputName.value(savedQuery.title)
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
}
