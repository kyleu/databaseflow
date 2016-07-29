package ui.modal

import java.util.UUID

import org.scalajs.jquery.{jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

object QueryExportFormManager {
  private[this] val modal = js.Dynamic.global.$("#export-modal")

  private[this] val inputQueryId = $("#input-export-query-id", modal)
  private[this] val inputSql = $("#input-export-sql", modal)
  private[this] val inputFilename = $("#input-export-filename", modal)

  def init() = {
    TemplateUtils.clickHandler($("#export-cancel-link", modal), (jq) => modal.closeModal())
  }

  def show(queryId: UUID, sql: String, filename: String) = {
    inputQueryId.value(queryId.toString)
    inputSql.value(sql)
    inputFilename.value(filename)
    modal.openModal()
  }
}
