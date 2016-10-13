package ui.modal

import java.util.UUID

import models.RowUpdate
import models.schema.Column
import models.template.tbl.RowUpdateTemplate
import org.scalajs.jquery.{jQuery => $}
import services.NotificationService
import utils.{NetworkMessage, TemplateUtils}

import scala.scalajs.js

object RowUpdateManager {
  private[this] val modal = js.Dynamic.global.$("#data-insert-modal")

  private[this] val modalContent = $("#data-insert-modal-content", modal)
  private[this] val linkInsert = $("#data-insert-save-link", modal)
  private[this] val linkCancel = $("#data-insert-cancel-link", modal)

  private[this] var activeMessage: Option[RowUpdate] = None
  private[this] var activeColumns: Option[Seq[Column]] = None

  def init() = {
    TemplateUtils.clickHandler(linkInsert, jq => {
      val msg = activeMessage.getOrElse(throw new IllegalStateException("Missing active InsertRow message."))
      val updated = msg.copy(params = getParams)
      NetworkMessage.sendMessage(updated)
    })
    TemplateUtils.clickHandler(linkCancel, jq => close())
  }

  def show(name: String, pk: Seq[(String, String)], columns: Seq[Column]) = {
    val resultId = UUID.randomUUID
    activeMessage = Some(RowUpdate(name, pk, Map.empty, resultId))
    activeColumns = Some(columns)
    val html = RowUpdateTemplate.forColumns(name, columns)
    modalContent.html(html.render)
    modal.openModal()
  }

  def close() = {
    modalContent.text("")
    activeMessage = None
    activeColumns = None
    modal.closeModal()
  }

  def handleInsertRowResponse(resultId: UUID, errors: Map[String, String]) = {
    if (errors.isEmpty) {
      NotificationService.info("Row Inserted", s"Added one new row ($resultId)")
      close()
    } else {
      $(".insert-row-error", modal).hide()
      errors.foreach { error =>
        $(s"#insert-row-error-${error._1}", modal).text(error._2).show()
      }
    }
  }

  private[this] def getParams = {
    val cols = activeColumns.getOrElse(throw new IllegalStateException("Missing active columns for insert."))
    val params = cols.flatMap { col =>
      val toggle = $(s"#insert-row-toggle-${col.name}", modal).prop("checked").toString.toBoolean
      utils.Logging.info(col.name + ": " + toggle)
      if (toggle) {
        val v = $(s"#insert-row-input-${col.name}", modal).value().toString
        Some(col.name -> v)
      } else {
        None
      }
    }.toMap
    params
  }
}
