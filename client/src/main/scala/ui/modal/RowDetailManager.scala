package ui.modal

import models.query.QueryResult
import models.template.tbl.RowDetailTemplate
import org.scalajs.jquery.{jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

object RowDetailManager {
  private[this] var activeTable: Option[String] = None
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

  def show(table: Option[String], pk: Seq[String], data: Seq[(QueryResult.Col, String)]) = {
    activeTable = table
    activePk = pk
    activeData = data

    val html = RowDetailTemplate.forData(data)
    modalContent.html(html.render)
    if (table.isEmpty || pk.isEmpty) {
      linkEdit.hide()
    } else {
      linkEdit.show()
    }
    modal.openModal()
  }

  def edit() = {
    val keyData = activePk.flatMap { col =>
      activeData.find(_._1.name.compareToIgnoreCase(col) == 0).map(_._2)
    }
    utils.Logging.info(s"Edit: [${activeTable.getOrElse("?")}] (${activePk.mkString(", ")}) = ${keyData.mkString(", ")}")
  }

  def close() = {
    modalContent.text("")
    modal.closeModal()
  }
}
