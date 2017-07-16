package services.schema

import java.util.UUID

import models.connection.ConnectionSettings
import models.schema.Schema
import models.user.User
import services.database.{DatabaseConnection, DatabaseRegistry}
import util.Logging

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

object SchemaService extends Logging {
  private[this] var schemaMap: Map[UUID, Schema] = Map.empty

  def get(connectionId: UUID) = schemaMap.get(connectionId)

  def set(connectionId: UUID, schema: Schema) = {
    schemaMap = schemaMap + (connectionId -> schema)
  }

  def getSchema(db: DatabaseConnection, forceRefresh: Boolean = false) = Try {
    schemaMap.get(db.connectionId) match {
      case Some(_) if forceRefresh =>
        val s = SchemaHelper.calculateSchema(db)
        set(db.connectionId, s)
        SchemaRefreshService.refreshSchema(db)
        s
      case Some(schema) => schema
      case None =>
        val s = SchemaHelper.calculateSchema(db)
        set(db.connectionId, s)
        s
    }
  }

  def getSchemaFor(user: User, cs: ConnectionSettings) = DatabaseRegistry.databaseForUser(user, cs.id) match {
    case Left(ex) => throw ex
    case Right(conn) => getSchema(conn) match {
      case Success(s) => s
      case Failure(ex) => throw ex
    }
  }

  def getSchemaWithDetails(cs: ConnectionSettings) = {
    val db = DatabaseRegistry.databaseFor(cs.id) match {
      case Left(ex) => throw ex
      case Right(x) => x
    }
    getSchema(db) match {
      case Success(schema) if schema.detailsLoadedAt.isDefined => Future.successful(schema)
      case Success(_) =>
        val promise = Promise[Schema]()
        def onSuccess(s: Schema): Unit = promise.complete(Success(s))
        def onFailure(t: Throwable): Unit = promise.complete(Failure(t))
        SchemaRefreshService.refreshSchema(db, onSuccess, onFailure)
        promise.future
    }
  }

  def getSchemaWithDetailsFor(user: User, cs: ConnectionSettings) = {
    getSchemaFor(user, cs) match {
      case schema if schema.detailsLoadedAt.isDefined => Future.successful(schema)
      case _ =>
        val promise = Promise[Schema]()
        def onSuccess(s: Schema): Unit = promise.complete(Success(s))
        def onFailure(t: Throwable): Unit = promise.complete(Failure(t))
        DatabaseRegistry.databaseForUser(user, cs.id) match {
          case Left(ex) => throw ex
          case Right(db) => SchemaRefreshService.refreshSchema(db, onSuccess, onFailure)
        }
        promise.future
    }
  }

  def getTable(connectionId: UUID, name: String) = get(connectionId).flatMap(_.tables.find(_.name == name))
  def getView(connectionId: UUID, name: String) = get(connectionId).flatMap(_.views.find(_.name == name))
  def getProcedure(connectionId: UUID, name: String) = get(connectionId).flatMap(_.procedures.find(_.name == name))
}
