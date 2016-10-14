package services.socket

import java.util.UUID

import models.ServerError
import models.queries.result.CachedResultQueries
import models.query.RowDataOptions
import services.database.core.MasterDatabase
import services.schema.SchemaService
import utils.Logging

trait RowDataHelper extends Logging { this: SocketService =>
  protected[this] def handleGetRowData(key: String, queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) = key match {
    case "table" => handleGetTableRowData(queryId, name, options, resultId)
    case "view" => handleGetViewRowData(queryId, name, options, resultId)
    case "cache" => handleGetCacheRowData(queryId, name, options, resultId)
  }

  private[this] def handleGetTableRowData(queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) = {
    SchemaService.getTable(connectionId, name) match {
      case Some(table) => handleShowDataResponse(queryId, "table", table.name, table.primaryKey, table.foreignKeys, options, resultId, cacheDb = false)
      case None =>
        log.warn(s"Attempted to show data for invalid table [$name].")
        out ! ServerError("Invalid Table", s"[$name] is not a valid table.")
    }
  }

  private[this] def handleGetViewRowData(queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) = {
    SchemaService.getView(connectionId, name) match {
      case Some(view) => handleShowDataResponse(queryId, "view", view.name, None, Nil, options, resultId, cacheDb = false)
      case None =>
        log.warn(s"Attempted to show data for invalid view [$name].")
        out ! ServerError("Invalid View", s"[$name] is not a valid view.")
    }
  }

  private[this] def handleGetCacheRowData(queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) = {
    MasterDatabase.query(CachedResultQueries.getById(resultId)) match {
      case Some(result) =>
        val (pk, fks) = result.source match {
          case Some(src) => SchemaService.getTable(result.connectionId, src) match {
            case Some(table) => table.primaryKey -> table.foreignKeys
            case None => None -> Nil
          }
          case None => None -> Nil
        }
        handleShowDataResponse(queryId, "cache", name, pk, fks, options, resultId, cacheDb = true)
      case None => throw new IllegalStateException(s"Unknown cached result [$resultId].")
    }
  }
}
