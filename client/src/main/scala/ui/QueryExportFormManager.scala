package ui

import java.util.UUID

import org.scalajs.jquery.{ jQuery => $ }

import scala.scalajs.js

object QueryExportFormManager {
  private[this] val modal = js.Dynamic.global.$("#export-modal")

  private[this] val inputQueryId = $("#input-export-query-id", modal)
  private[this] val inputSql = $("#input-export-sql", modal)

  def init() = {
    utils.JQueryUtils.clickHandler($("#export-cancel-link", modal), (jq) => modal.closeModal())
  }

  def show(queryId: UUID, sql: String) = {
    inputQueryId.value(queryId.toString)
    inputSql.value(sql)
    modal.openModal()
  }
}
