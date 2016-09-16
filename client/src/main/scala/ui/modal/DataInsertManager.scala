package ui.modal

import java.util.UUID

import models.InsertRow
import models.schema.Column
import models.template.tbl.InsertRowTemplate
import org.scalajs.jquery.{jQuery => $}
import services.NotificationService
import utils.{NetworkMessage, TemplateUtils}

import scala.scalajs.js

object DataInsertManager {
  private[this] val modal = js.Dynamic.global.$("#data-insert-modal")

  private[this] val confirmContent = $("#data-insert-modal-content", modal)
  private[this] val linkInsert = $("#data-insert-save-link", modal)
  private[this] val linkCancel = $("#data-insert-cancel-link", modal)

  private[this] var activeMessage: Option[InsertRow] = None
  private[this] var activeColumns: Option[Seq[Column]] = None

  def init() = {
    TemplateUtils.clickHandler(linkInsert, jq => {
      val msg = activeMessage.getOrElse(throw new IllegalStateException("Missing active InsertRow message."))
      val updated = msg.copy(params = getParams)
      NetworkMessage.sendMessage(updated)
    })
    TemplateUtils.clickHandler(linkCancel, jq => close())
  }

  def show(queryId: UUID, name: String, columns: Seq[Column]) = {
    val resultId = UUID.randomUUID
    activeMessage = Some(InsertRow(name, Map.empty, resultId))
    activeColumns = Some(columns)
    val html = InsertRowTemplate.forColumns(name, columns)
    confirmContent.html(html.render)
    modal.openModal()
  }

  def close() = {
    confirmContent.text("")
    activeMessage = None
    activeColumns = None
    modal.closeModal()
  }

  def handleInsertRowResponse(resultId: UUID, errors: Map[String, String]) = {
    if (errors.isEmpty) {
      NotificationService.info("Row Inserted", "Added one new row")
      close()
    } else {
      $(s".insert-row-error", modal).hide()
      errors.foreach { error =>
        $(s"#insert-row-error-${error._1}", modal).text(error._2).show()
      }
    }
  }

  private[this] def getParams = {
    val cols = activeColumns.getOrElse(throw new IllegalStateException("Missing active columns for insert."))
    val params = cols.map { col =>
      val v = $(s"#insert-row-input-${col.name}", modal).value().toString
      col.name -> v
    }.toMap
    params
  }
}
