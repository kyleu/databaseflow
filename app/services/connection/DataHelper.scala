package services.connection

import java.util.UUID

import models._
import models.engine.EngineQueries
import models.queries.DynamicQuery
import models.query.QueryResult
import models.schema.{ Table, View }
import services.schema.SchemaService
import utils.{ DateUtils, Logging }

trait DataHelper extends Logging { this: ConnectionService =>
  protected[this] def handleGetTableRowData(queryId: UUID, name: String) = SchemaService.getTable(connectionId, name) match {
    case Some(table) => handleShowTableDataResponse(queryId, table)
    case None =>
      log.warn(s"Attempted to show data for invalid table [$name].")
      out ! ServerError("Invalid Table", s"[$name] is not a valid table.")
  }

  protected[this] def handleGetViewRowData(queryId: UUID, name: String) = SchemaService.getView(connectionId, name) match {
    case Some(view) => handleShowViewDataResponse(queryId, view)
    case None =>
      log.warn(s"Attempted to show data for invalid view [$name].")
      out ! ServerError("Invalid Table", s"[$name] is not a valid view.")
  }

  private[this] def handleShowTableDataResponse(queryId: UUID, table: Table) {
    val id = UUID.randomUUID
    val startMs = DateUtils.nowMillis
    val sql = EngineQueries.selectFrom(table.name, limit = Some(1001))(db.engine)
    log.info(s"Showing data for [${table.name}] using sql [$sql].")
    sqlCatch(queryId, sql, startMs) { () =>
      val result = db.executeUnknown(DynamicQuery(sql))
      val (columns, data) = result match {
        case Left(rs) => rs.cols -> rs.data
        case Right(i) => throw new IllegalStateException(s"Invalid query [$sql] returned statement result.")
      }

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

  private[this] def handleShowViewDataResponse(queryId: UUID, view: View) {
    val id = UUID.randomUUID
    val startMs = DateUtils.nowMillis
    val sql = EngineQueries.selectFrom(view.name, limit = Some(1001))(db.engine)
    log.info(s"Showing data for [${view.name}] using sql [$sql].")
    sqlCatch(queryId, sql, startMs) { () =>
      val result = db.executeUnknown(DynamicQuery(sql))

      val (columns, data) = result match {
        case Left(rs) => rs.cols -> rs.data
        case Right(i) => throw new IllegalStateException(s"Invalid query [$sql] returned statement result.")
      }
      //log.info(s"Query result: [$result].")
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryResultResponse(id, QueryResult(
        queryId = queryId,
        title = view.name,
        sql = sql,
        columns = columns,
        data = data,
        sortable = true,
        occurred = startMs
      ), durationMs)
    }
  }
}
