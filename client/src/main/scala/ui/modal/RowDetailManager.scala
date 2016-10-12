package ui.modal

import models.query.QueryResult
import models.template.tbl.RowDetailTemplate
import org.scalajs.dom
import org.scalajs.jquery.{JQuery, jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

object RowDetailManager {
  def dataClickHandler(jq: JQuery, result: QueryResult) = {
    var tdData = Seq.empty[String]
    $("td", jq.parent().parent()).map { e: dom.Element =>
      val v = $(e).data("v").toString
      if (v != "undefined") {
        tdData = tdData :+ v
      }
    }
    val data = result.columns.zip(tdData)
    val pk = data.filter(_._1.primaryKey).map(_._1.name)
    utils.Logging.info(s"Showing row [X] (PK: ${pk.mkString(", ")}) with data [${data.map(d => d._1.name + ": " + d._2).mkString(", ")}].")
    show(data)
  }

  private[this] val modal = js.Dynamic.global.$("#row-detail-modal")

  private[this] val modalContent = $("#row-detail-modal-content", modal)
  private[this] val linkEdit = $("#row-detail-edit-link", modal)
  private[this] val linkOk = $("#row-detail-ok-link", modal)

  def init() = {
    TemplateUtils.clickHandler(linkEdit, jq => {
      utils.Logging.info("Row edit request.")
    })
    TemplateUtils.clickHandler(linkOk, jq => close())
  }

  def show(data: Seq[(QueryResult.Col, String)]) = {
    val html = RowDetailTemplate.forData(data)
    modalContent.html(html.render)
    modal.openModal()
  }

  def close() = {
    modalContent.text("")
    modal.closeModal()
  }
}
