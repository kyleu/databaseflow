package ui.query

import java.util.UUID

import models.GetRowData
import models.query.{QueryResult, RowDataOptions}
import ui.ProgressManager
import util.{Logging, NetworkMessage}

object RowDataManager {
  def showRowData(key: QueryResult.SourceType, queryId: UUID, name: String, options: RowDataOptions, resultId: UUID): Unit = {
    Logging.debug(s"Showing [$key] row data for [$name] with options [$options].")
    if (options.offset.forall(_ == 0)) {
      ProgressManager.startProgress(queryId, resultId, name)
    }
    NetworkMessage.sendMessage(GetRowData(key, queryId, name, options, resultId))
  }
}
