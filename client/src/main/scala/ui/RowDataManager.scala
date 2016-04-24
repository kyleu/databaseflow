package ui

import java.util.UUID

import models.{ GetTableRowData, GetViewRowData }
import models.query.RowDataOptions
import models.template._
import org.scalajs.jquery.{ jQuery => $ }
import utils.JQueryUtils

object RowDataManager {
  private[this] var openRowData = Map.empty[String, UUID]

  def showTableRowData(queryId: UUID, name: String, options: RowDataOptions): Unit = {
    val resultId = UUID.randomUUID

    def onComplete(): Unit = {
      val panel = $(s"#$resultId")
      if (panel.length != 1) {
        throw new IllegalStateException(s"Found [${panel.length}] panels for table result [$resultId].")
      }
      JQueryUtils.clickHandler($(".sorted-title", panel), (j) => {
        val col = j.data("col").toString
        val asc = j.data("dir").toString == "asc"

        showTableRowData(queryId, name, options.copy(
          orderByCol = Some(col),
          orderByAsc = Some(!asc)
        ))
      })
    }

    ProgressManager.startProgress(queryId, resultId, onComplete, Icons.loading, name)
    utils.NetworkMessage.sendMessage(GetTableRowData(queryId = queryId, name = name, options = options, resultId = resultId))
  }

  def showViewRowData(queryId: UUID, name: String, options: RowDataOptions): Unit = {
    val resultId = UUID.randomUUID

    def onComplete(): Unit = {
      val panel = $(s"#$resultId")
      if (panel.length != 1) {
        throw new IllegalStateException(s"Found [${panel.length}] panels for result [$resultId].")
      }
      JQueryUtils.clickHandler($(".sorted-title", panel), (j) => {
        val col = j.data("col").toString
        val asc = j.data("dir").toString == "asc"

        showViewRowData(queryId, name, options.copy(
          orderByCol = Some(col),
          orderByAsc = Some(!asc)
        ))
      })
    }

    ProgressManager.startProgress(queryId, resultId, onComplete, Icons.loading, name)
    utils.NetworkMessage.sendMessage(GetViewRowData(queryId = queryId, name = name, options = options, resultId = resultId))
  }

}
