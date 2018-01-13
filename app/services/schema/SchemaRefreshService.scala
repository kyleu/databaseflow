package services.schema

import java.util.UUID

import models.schema.Schema
import services.database.{DatabaseConnection, DatabaseWorkerPool}
import util.Logging

import scala.concurrent.Future
import scala.util.Try
import util.FutureUtils.defaultContext

object SchemaRefreshService extends Logging {
  private[this] var refreshCount = 0
  private[this] val activeRefreshes = collection.mutable.HashMap.empty[UUID, Future[Schema]]

  def refreshSchema(db: DatabaseConnection, onSuccess: (Schema) => Unit = _ => Unit, onFailure: (Throwable) => Unit = _ => Unit) = Try {
    SchemaService.get(db.connectionId) match {
      case Some(schema) if activeRefreshes.isDefinedAt(db.connectionId) =>
        log.info(s"Awaiting current refresh of schema [${schema.connectionId}]: $refreshCount.")
        val f = activeRefreshes(db.connectionId).map { s =>
          onSuccess(s)
          s
        }
        f.failed.foreach(onFailure)
        f
      case Some(schema) =>
        val startMs = System.currentTimeMillis
        def work() = db.withConnection { conn =>
          refreshCount = refreshCount + 1
          log.info(s"Refreshing schema [${schema.schemaName.getOrElse(schema.connectionId)}]: $refreshCount.")
          val enums = MetadataEnums.getEnums(db)
          val metadata = conn.getMetaData
          schema.copy(
            enums = enums,
            tables = MetadataTables.withTableDetails(db, conn, metadata, schema.tables, enums),
            views = MetadataViews.withViewDetails(db, conn, metadata, schema.views, enums),
            procedures = MetadataProcedures.withProcedureDetails(metadata, schema.catalog, schema.schemaName, schema.procedures, enums),
            detailsLoadedAt = Some(System.currentTimeMillis)
          )
        }

        def onSuccessMapped(schema: Schema) = {
          SchemaService.set(db.connectionId, schema)
          log.info(s"Schema update complete for [${schema.schemaName.getOrElse(schema.connectionId)}] in [${System.currentTimeMillis - startMs}ms].")
          log.info(s"Processed details for [${schema.enums.size}] enums, [${schema.tables.size}] tables, and [${schema.views.size}] views.")
          activeRefreshes.remove(db.connectionId)
          onSuccess(schema)
        }

        def onFailureWrapped(t: Throwable) = {
          log.warn(s"Unable to refresh schema for [${db.name}].", t)
          activeRefreshes.remove(db.connectionId)
          onFailure(t)
        }

        val f = DatabaseWorkerPool.submitWork(work _, onSuccessMapped, onFailureWrapped)
        activeRefreshes(db.connectionId) = f
        f
      case None => throw new IllegalStateException(s"Attempted to refresh schema [$db.connectionId], which is not loaded.")
    }
  }
}
