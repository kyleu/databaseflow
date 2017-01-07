package services.schema

import java.util.UUID

import models.connection.ConnectionSettings
import models.schema.Schema
import models.user.User
import services.database.{DatabaseConnection, DatabaseRegistry, DatabaseWorkerPool}
import utils.Logging

import scala.util.{Failure, Success, Try}

object SchemaService extends Logging {
  private[this] var schemaMap: Map[UUID, Schema] = Map.empty
  private[this] var refreshCount = 0

  def getSchema(db: DatabaseConnection, forceRefresh: Boolean = false) = Try {
    schemaMap.get(db.connectionId) match {
      case Some(schema) if forceRefresh =>
        val s = SchemaHelper.calculateSchema(db)
        schemaMap = schemaMap + (db.connectionId -> s)
        refreshSchema(db)
        s
      case Some(schema) => schema
      case None =>
        val s = SchemaHelper.calculateSchema(db)
        schemaMap = schemaMap + (db.connectionId -> s)
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

  def refreshSchema(db: DatabaseConnection, onSuccess: (Schema) => Unit = (s) => Unit, onFailure: (Throwable) => Unit = (x) => Unit) = Try {
    schemaMap.get(db.connectionId) match {
      case Some(schema) =>
        val startMs = System.currentTimeMillis
        def work() = db.withConnection { conn =>
          refreshCount = refreshCount + 1
          log.info(s"Refreshing schema [${schema.schemaName.getOrElse(schema.connectionId)}]: $refreshCount.")
          val metadata = conn.getMetaData
          schema.copy(
            tables = MetadataTables.withTableDetails(db, conn, metadata, schema.tables),
            views = MetadataViews.withViewDetails(db, conn, metadata, schema.views),
            procedures = MetadataProcedures.withProcedureDetails(metadata, schema.catalog, schema.schemaName, schema.procedures),
            detailsLoadedAt = Some(System.currentTimeMillis)
          )
        }

        def onSuccessMapped(schema: Schema) = {
          schemaMap = schemaMap + (db.connectionId -> schema)
          log.info(s"Schema update complete for [${schema.schemaName.getOrElse(schema.connectionId)}] in [${System.currentTimeMillis - startMs}ms].")
          onSuccess(schema)
        }

        DatabaseWorkerPool.submitWork(work, onSuccessMapped, onFailure)
      case None => throw new IllegalStateException(s"Attempted to refresh schema [$db.connectionId], which is not loaded.")
    }
  }

  def getTable(connectionId: UUID, name: String) = schemaMap.get(connectionId).flatMap(_.tables.find(_.name == name))
  def getView(connectionId: UUID, name: String) = schemaMap.get(connectionId).flatMap(_.views.find(_.name == name))
  def getProcedure(connectionId: UUID, name: String) = schemaMap.get(connectionId).flatMap(_.procedures.find(_.name == name))
}
