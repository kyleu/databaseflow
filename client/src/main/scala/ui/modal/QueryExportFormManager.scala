package ui.modal

import java.util.UUID

import models.query.QueryResult
import org.scalajs.jquery.{jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

import upickle.default._

object QueryExportFormManager {
  private[this] val modal = js.Dynamic.global.$("#export-modal")

  private[this] val inputQueryId = $("#input-export-query-id", modal)
  private[this] val inputSource = $("#input-export-source", modal)

  def init() = {
    TemplateUtils.clickHandler($("#export-cancel-link", modal), jq => modal.closeModal())
  }

  def show(queryId: UUID, source: QueryResult.Source, filename: String) = {
    inputQueryId.value(queryId.toString)
    inputSource.value(write(source))
    modal.openModal()
  }
}
