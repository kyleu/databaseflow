package ui.query

import java.util.UUID

import models.GetRowData
import models.query.RowDataOptions
import org.scalajs.jquery.{ jQuery => $ }
import ui.ProgressManager
import utils.JQueryUtils

object RowDataManager {
  def showRowData(key: String, queryId: UUID, name: String, options: RowDataOptions, resultId: UUID = UUID.randomUUID): Unit = {
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
        val limit = appendRowsLink.data("limit").toString.toInt
        val offset = appendRowsLink.data("offset").toString.toInt match {
          case 0 => 100
          case x => x
        }
        val newOptions = options.copy(limit = Some(limit), offset = Some(offset))
        utils.Logging.info(s"Requesting additional rows from offset [${newOptions.offset.getOrElse(0)}] and limit [$limit].")
        appendRowsLink.data("offset", offset + limit)
        appendRowsLink.hide()
        showRowData(key, queryId, name, newOptions, resultId)
      })
    }

    if (options.offset.forall(_ == 0)) {
      ProgressManager.startProgress(queryId, resultId, onComplete, name)
    }
    utils.NetworkMessage.sendMessage(GetRowData(key, queryId, name, options, resultId))
  }
}
