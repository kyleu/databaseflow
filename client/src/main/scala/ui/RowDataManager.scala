package ui

import java.util.UUID

import models.{ GetTableRowData, GetViewRowData }
import models.query.RowDataOptions
import org.scalajs.jquery.{ jQuery => $ }
import utils.JQueryUtils

object RowDataManager {
  def showTableRowData(queryId: UUID, name: String, options: RowDataOptions) = showRowData("table", queryId, name, options)
  def showViewRowData(queryId: UUID, name: String, options: RowDataOptions) = showRowData("view", queryId, name, options)

  private[this] def showRowData(key: String, queryId: UUID, name: String, options: RowDataOptions, resultId: UUID = UUID.randomUUID): Unit = {
    utils.Logging.info(s"Showing [$key] row data for [$name] with options [$options].")

    def onComplete(): Unit = {
      val panel = $(s"#$resultId")
      if (panel.length != 1) {
        throw new IllegalStateException(s"Found [${panel.length}] panels for $key result [$resultId].")
      }
      JQueryUtils.clickHandler($(".sorted-title", panel), (j) => {
        val col = j.data("col").toString
        val asc = j.data("dir").toString == "asc"
        val newOptions = options.copy(orderByCol = Some(col), orderByAsc = Some(!asc))
        showRowData(key, queryId, name, newOptions)
      })

      val appendRowsLink = $(".append-rows-link", panel)
      JQueryUtils.clickHandler(appendRowsLink, (j) => {
        val offset = appendRowsLink.data("offset").toString.toInt + UserManager.rowsReturned
        val newOptions = options.copy(limit = Some(UserManager.rowsReturned), offset = Some(offset))
        utils.Logging.info(s"Requesting additional rows from offset [${newOptions.offset.getOrElse(0)}].")
        appendRowsLink.data("offset", offset.toString)
        appendRowsLink.hide()
        showRowData(key, queryId, name, newOptions, resultId)
      })
    }

    if (options.offset.forall(_ == 0)) {
      ProgressManager.startProgress(queryId, resultId, onComplete, name)
    }
    val msg = key match {
      case "table" => GetTableRowData(queryId, name, options, resultId)
      case "view" => GetViewRowData(queryId, name, options, resultId)
    }
    utils.NetworkMessage.sendMessage(msg)
  }
}
