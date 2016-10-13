package ui.modal

import models.query.QueryResult
import models.template.tbl.RowDetailTemplate
import org.scalajs.jquery.{jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

object RowDetailManager {
  private[this] var activePk: Seq[String] = Nil
  private[this] var activeData: Seq[(QueryResult.Col, String)] = Nil

  private[this] val modal = js.Dynamic.global.$("#row-detail-modal")

  private[this] val modalContent = $("#row-detail-modal-content", modal)
  private[this] val linkEdit = $("#row-detail-edit-link", modal)
  private[this] val linkOk = $("#row-detail-ok-link", modal)

  def init() = {
    TemplateUtils.clickHandler(linkEdit, jq => edit())
    TemplateUtils.clickHandler(linkOk, jq => close())
  }

  def show(pk: Seq[String], data: Seq[(QueryResult.Col, String)]) = {
    activePk = pk
    activeData = data

    val html = RowDetailTemplate.forData(data)
    modalContent.html(html.render)
    if (pk.isEmpty) {
      linkEdit.hide()
    } else {
      linkEdit.show()
    }
    modal.openModal()
  }

  def edit() = {
    utils.Logging.info("Edit!")
  }

  def close() = {
    modalContent.text("")
    modal.closeModal()
  }
}
