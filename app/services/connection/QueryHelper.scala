package services.connection

import java.util.UUID

import models.queries.DynamicQuery
import models.query.{ QueryResult, SavedQuery }
import models.schema.Table
import models.{ QueryDeleteResponse, QueryResultResponse, QuerySaveResponse, ServerError }
import services.database.MasterDatabase
import services.query.SavedQueryService
import services.schema.SchemaService
import utils.{ DateUtils, Logging }

import scala.util.control.NonFatal

trait QueryHelper extends Logging { this: ConnectionService =>
  def attemptConnect() = MasterDatabase.databaseFor(connectionId) match {
    case Right(d) =>
      Some(d)
    case Left(x) =>
      log.warn("Error attempting to connect to database.", x)
      out ! ServerError("Database Connect Failed", x.getMessage)
      None
  }

  protected[this] def handleQuerySaveRequest(sq: SavedQuery) = {
    log.info(s"Saving query as [${sq.id}].")
    try {
      val result = SavedQueryService.save(sq, Some(user.id))
      out ! QuerySaveResponse(savedQuery = result)
    } catch {
      case NonFatal(x) => out ! QuerySaveResponse(error = Some(x.getMessage), savedQuery = sq)
    }
  }

  protected[this] def handleQueryDeleteRequest(id: UUID) = {
    log.info(s"Deleting query [$id].")
    try {
      val result = SavedQueryService.delete(id, Some(user.id))
      out ! QueryDeleteResponse(id = id)
    } catch {
      case NonFatal(x) => out ! QueryDeleteResponse(id = id, error = Some(x.getMessage))
    }
  }

  protected[this] def handleRunQuery(queryId: UUID, sql: String) = {
    log.info(s"Performing query action [run] for sql [$sql].")
    val id = UUID.randomUUID
    val startMs = DateUtils.nowMillis
    sqlCatch(queryId, sql, startMs) { () =>
      val result = db.query(DynamicQuery(sql))
      //log.info(s"Query result: [$result].")
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryResultResponse(id, QueryResult(
        queryId = queryId,
        title = "Query Results",
        sql = sql,
        columns = result._1,
        data = result._2,
        sortable = false,
        occurred = startMs
      ), durationMs)
    }
  }

  protected[this] def handleGetTableRowData(queryId: UUID, name: String) = {
    SchemaService.getTable(name) match {
      case Some(table) => handleShowTableDataResponse(queryId, table)
      case None => SchemaService.getView(name) match {
        case Some(view) => handleShowTableDataResponse(queryId, view)
        case None =>
          log.warn(s"Attempted to view invalid table or view [$name].")
          out ! ServerError("Invalid Table", s"[$name] is not a valid table or view.")
      }
    }
  }

  private[this] def handleShowTableDataResponse(queryId: UUID, table: Table) {
    log.info(s"Showing data for [${table.name}].")
    val id = UUID.randomUUID
    val startMs = DateUtils.nowMillis
    val sql = s"select * from ${table.name} limit 1001"
    sqlCatch(queryId, sql, startMs) { () =>
      val (columns, data) = db.query(DynamicQuery(sql))
      val columnsWithRelations = columns.map { col =>
        table.foreignKeys.find(_.references.exists(_.source == col.name)) match {
          case Some(fk) => col.copy(
            relationTable = Some(fk.targetTable),
            relationColumn = fk.references.find(_.source == col.name).map(_.target)
          )
          case None => col
        }
      }

      //log.info(s"Query result: [$result].")
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryResultResponse(id, QueryResult(
        queryId = queryId,
        title = table.name,
        sql = sql,
        columns = columnsWithRelations,
        data = data,
        sortable = true,
        occurred = startMs
      ), durationMs)
    }
  }
}
