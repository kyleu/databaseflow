package ui.query

import java.util.UUID

import models.GetRowData
import models.query.RowDataOptions
import ui.ProgressManager

object RowDataManager {
  def showRowData(key: String, queryId: UUID, name: String, options: RowDataOptions, resultId: UUID = UUID.randomUUID): Unit = {
    utils.Logging.info(s"Showing [$key] row data for [$name] with options [$options].")

    if (options.offset.forall(_ == 0)) {
      ProgressManager.startProgress(queryId, resultId, name)
    }
    utils.NetworkMessage.sendMessage(GetRowData(key, queryId, name, options, resultId))
  }
}
