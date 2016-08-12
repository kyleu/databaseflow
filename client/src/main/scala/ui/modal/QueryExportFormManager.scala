package ui.modal

import java.util.UUID

import org.scalajs.jquery.{jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

object QueryExportFormManager {
  private[this] val modal = js.Dynamic.global.$("#export-modal")

  private[this] val inputQueryId = $("#input-export-query-id", modal)
  private[this] val inputResultId = $("#input-export-result-id", modal)
  private[this] val inputFilename = $("#input-export-filename", modal)

  def init() = {
    TemplateUtils.clickHandler($("#export-cancel-link", modal), (jq) => modal.closeModal())
  }

  def show(queryId: UUID, resultId: UUID, filename: String) = {
    inputQueryId.value(queryId.toString)
    inputResultId.value(resultId.toString)
    inputFilename.value(filename)
    modal.openModal()
  }
}
