package services.query

import java.util.UUID

import akka.actor.ActorRef
import models._
import models.database.Queryable
import models.engine.DatabaseEngine
import models.queries.result.CachedResultQueries
import models.query.{QueryResult, RowDataOptions}
import models.schema.{ForeignKey, PrimaryKey}
import models.user.User
import services.database.DatabaseRegistry
import services.database.core.{MasterDatabase, ResultCacheDatabase}
import services.schema.SchemaService
import util.FutureUtils.defaultContext
import util.Logging

import scala.concurrent.Future

object RowDataService extends Logging {
  case class Params(
    queryId: UUID, t: String, name: String, pk: Option[PrimaryKey], keys: Seq[ForeignKey], columns: Seq[String], options: RowDataOptions, resultId: UUID
  )
  case class Config(queryId: UUID, name: String, columns: Seq[String], options: RowDataOptions, resultId: UUID, out: Option[ActorRef])

  def getRowData(user: User, connectionId: UUID, key: QueryResult.SourceType, name: String, columns: Seq[String], options: RowDataOptions) = {
    val dbAccess = DatabaseRegistry.databaseForUser(user, connectionId) match {
      case Right(database) => (connectionId, database, database.engine)
      case Left(x) => throw x
    }
    handleGetRowData(dbAccess, key, Config(UUID.randomUUID, name, columns, options, UUID.randomUUID, None)).map {
      case qrr: QueryResultResponse => qrr.result
      case se: ServerError => throw new IllegalStateException(se.reason + ": " + se.content)
      case x => throw new IllegalStateException(x.toString)
    }
  }

  def handleGetRowData(dbAccess: (UUID, Queryable, DatabaseEngine), key: QueryResult.SourceType, config: Config) = {
    val (conn, db, engine) = dbAccess
    key match {
      case QueryResult.SourceType.Table => handleGetTableRowData(conn, db, engine, config)
      case QueryResult.SourceType.View => handleGetViewRowData(conn, db, engine, config)
      case QueryResult.SourceType.Cache => handleGetCacheRowData(config)
    }
  }

  private[this] def handleGetTableRowData(conn: UUID, db: Queryable, engine: DatabaseEngine, config: Config) = {
    SchemaService.getTable(conn, config.name) match {
      case Some(table) =>
        val params = Params(config.queryId, "table", table.name, table.primaryKey, table.foreignKeys, config.columns, config.options, config.resultId)
        RowDataHelper.showDataResponse(params, db, engine, config.out)
      case None =>
        log.warn(s"Attempted to show data for invalid table [${config.name}].")
        val msg = ServerError("Invalid Table", s"[${config.name}] is not a valid table.")
        config.out.foreach(_ ! msg)
        Future.successful(msg)
    }
  }

  private[this] def handleGetViewRowData(conn: UUID, db: Queryable, engine: DatabaseEngine, config: Config) = {
    SchemaService.getView(conn, config.name) match {
      case Some(view) =>
        val params = Params(config.queryId, "view", view.name, None, Nil, config.columns, config.options, config.resultId)
        RowDataHelper.showDataResponse(params, db, engine, config.out)
      case None =>
        log.warn(s"Attempted to show data for invalid view [${config.name}].")
        val msg = ServerError("Invalid View", s"[${config.name}] is not a valid view.")
        config.out.foreach(_ ! msg)
        Future.successful(msg)
    }
  }

  private[this] def handleGetCacheRowData(config: Config) = {
    MasterDatabase.query(CachedResultQueries.getById(config.resultId)) match {
      case Some(result) =>
        val (pk, fks) = result.source match {
          case Some(src) => SchemaService.getTable(result.connectionId, src) match {
            case Some(table) => table.primaryKey -> table.foreignKeys
            case None => None -> Nil
          }
          case None => None -> Nil
        }
        val params = Params(config.queryId, "cache", config.name, pk, fks, config.columns, config.options, config.resultId)
        RowDataHelper.showDataResponse(params, ResultCacheDatabase.conn, ResultCacheDatabase.conn.engine, config.out)
      case None =>
        log.warn(s"Attempted to show data for invalid view [${config.name}].")
        val msg = ServerError("Invalid Cached Result", s"Unknown cached result [${config.resultId}].")
        config.out.foreach(_ ! msg)
        Future.successful(msg)
    }
  }
}
