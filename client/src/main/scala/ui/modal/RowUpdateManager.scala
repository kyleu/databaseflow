package ui.modal

import java.util.UUID

import models.RowUpdate
import models.schema.Column
import models.template.tbl.RowUpdateTemplate
import org.scalajs.jquery.{jQuery => $}
import services.NotificationService
import utils.{Messages, NetworkMessage, TemplateUtils}

import scala.scalajs.js

object RowUpdateManager {
  private[this] val modal = js.Dynamic.global.$("#row-update-modal")

  private[this] val modalContent = $("#row-update-modal-content", modal)
  private[this] val linkInsert = $("#row-update-save-link", modal)
  private[this] val linkCancel = $("#row-update-cancel-link", modal)

  private[this] var activeMessage: Option[RowUpdate] = None
  private[this] var activeColumns: Option[Seq[Column]] = None

  def init() = {
    TemplateUtils.clickHandler(linkInsert, _ => {
      val msg = activeMessage.getOrElse(throw new IllegalStateException("Missing active InsertRow message."))
      val updated = msg.copy(params = getParams)
      NetworkMessage.sendMessage(updated)
    })
    TemplateUtils.clickHandler(linkCancel, _ => close())
  }

  def show(insert: Boolean, name: String, pk: Seq[(String, String)], columns: Seq[Column], data: Map[String, String]) = {
    if (insert) { linkInsert.html(Messages("query.insert")) } else { linkInsert.html(Messages("query.update")) }

    val resultId = UUID.randomUUID
    activeMessage = Some(RowUpdate(name, pk, Map.empty, resultId))
    activeColumns = Some(columns)
    val html = RowUpdateTemplate.forColumns(insert, name, columns, data)
    modalContent.html(html.render)
    TemplateUtils.keyUpHandler($(".row-update-input", modalContent), (jq, _) => {
      if (jq.value().toString.nonEmpty) {
        $(s"#row-update-toggle-${jq.data("col")}").attr("checked", "checked")
      }
    })
    modal.openModal()
  }

  def close() = {
    modalContent.text("")
    activeMessage = None
    activeColumns = None
    modal.closeModal()
  }

  def handleRowUpdateResponse(pk: Seq[(String, String)], rowsAffected: Int, errors: Map[String, String]) = {
    if (errors.isEmpty) {
      val (k, msg) = if (pk.isEmpty) {
        "Row Inserted" -> "Added one new row."
      } else {
        "Row Updated" -> s"${utils.NumberUtils.toWords(rowsAffected, properCase = true)} row(s) affected."
      }
      NotificationService.info(k, msg)
      close()
    } else {
      $(".row-update-error", modal).hide()
      errors.foreach { error =>
        $(s"#row-update-error-${error._1}", modal).text(error._2).show()
      }
    }
  }

  private[this] def getParams = {
    val cols = activeColumns.getOrElse(throw new IllegalStateException("Missing active columns for insert."))
    val params = cols.flatMap { col =>
      val toggle = $(s"#row-update-toggle-${col.name}", modal).prop("checked").toString.toBoolean
      if (toggle) {
        val v = $(s"#row-update-input-${col.name}", modal).value().toString
        Some(col.name -> v)
      } else {
        None
      }
    }.toMap
    params
  }
}
