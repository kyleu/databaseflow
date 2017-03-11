package services.query

import java.util.UUID

import akka.actor.ActorRef
import models._
import models.database.Queryable
import models.engine.DatabaseEngine
import models.queries.result.CachedResultQueries
import models.query.RowDataOptions
import models.schema.{ForeignKey, PrimaryKey}
import models.user.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.database.DatabaseRegistry
import services.database.core.{MasterDatabase, ResultCacheDatabase}
import services.schema.SchemaService
import utils.Logging

import scala.concurrent.Future

object RowDataService extends Logging {
  case class Params(queryId: UUID, t: String, name: String, pk: Option[PrimaryKey], keys: Seq[ForeignKey], options: RowDataOptions, resultId: UUID)

  def getRowData(user: User, connectionId: UUID, key: String, name: String, options: RowDataOptions) = {
    val dbAccess = DatabaseRegistry.databaseForUser(user, connectionId) match {
      case Right(database) => (connectionId, database, database.engine)
      case Left(x) => throw x
    }
    handleGetRowData(dbAccess, key, UUID.randomUUID, name, options, UUID.randomUUID, None).map {
      case qrr: QueryResultResponse => qrr.result
      case se: ServerError => throw new IllegalStateException(se.reason + ": " + se.content)
      case x => throw new IllegalStateException(x.toString)
    }
  }

  def handleGetRowData(
    dbAccess: (UUID, Queryable, DatabaseEngine), key: String, queryId: UUID, name: String, options: RowDataOptions, resultId: UUID, out: Option[ActorRef]
  ) = {
    val (conn, db, engine) = dbAccess
    key match {
      case "table" => handleGetTableRowData(conn, db, engine, queryId, name, options, resultId, out)
      case "view" => handleGetViewRowData(conn, db, engine, queryId, name, options, resultId, out)
      case "cache" => handleGetCacheRowData(queryId, name, options, resultId, out)
    }
  }

  private[this] def handleGetTableRowData(
    conn: UUID, db: Queryable, engine: DatabaseEngine, queryId: UUID, name: String, options: RowDataOptions, resultId: UUID, out: Option[ActorRef]
  ) = {
    SchemaService.getTable(conn, name) match {
      case Some(table) =>
        val params = Params(queryId, "table", table.name, table.primaryKey, table.foreignKeys, options, resultId)
        RowDataHelper.showDataResponse(params, db, engine, out)
      case None =>
        log.warn(s"Attempted to show data for invalid table [$name].")
        val msg = ServerError("Invalid Table", s"[$name] is not a valid table.")
        out.foreach(_ ! msg)
        Future.successful(msg)
    }
  }

  private[this] def handleGetViewRowData(
    conn: UUID, db: Queryable, engine: DatabaseEngine, queryId: UUID, name: String, options: RowDataOptions, resultId: UUID, out: Option[ActorRef]
  ) = {
    SchemaService.getView(conn, name) match {
      case Some(view) =>
        val params = Params(queryId, "view", view.name, None, Nil, options, resultId)
        RowDataHelper.showDataResponse(params, db, engine, out)
      case None =>
        log.warn(s"Attempted to show data for invalid view [$name].")
        val msg = ServerError("Invalid View", s"[$name] is not a valid view.")
        out.foreach(_ ! msg)
        Future.successful(msg)
    }
  }

  private[this] def handleGetCacheRowData(queryId: UUID, name: String, options: RowDataOptions, resultId: UUID, out: Option[ActorRef]) = {
    MasterDatabase.query(CachedResultQueries.getById(resultId)) match {
      case Some(result) =>
        val (pk, fks) = result.source match {
          case Some(src) => SchemaService.getTable(result.connectionId, src) match {
            case Some(table) => table.primaryKey -> table.foreignKeys
            case None => None -> Nil
          }
          case None => None -> Nil
        }
        val params = Params(queryId, "cache", name, pk, fks, options, resultId)
        RowDataHelper.showDataResponse(params, ResultCacheDatabase.conn, ResultCacheDatabase.conn.engine, out)
      case None =>
        log.warn(s"Attempted to show data for invalid view [$name].")
        val msg = ServerError("Invalid Cached Result", s"Unknown cached result [$resultId].")
        out.foreach(_ ! msg)
        Future.successful(msg)
    }
  }
}
