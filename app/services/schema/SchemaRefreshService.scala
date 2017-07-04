package services.schema

import java.util.UUID

import models.schema.Schema
import services.database.{DatabaseConnection, DatabaseWorkerPool}
import utils.Logging

import scala.concurrent.Future
import scala.util.Try
import utils.FutureUtils.defaultContext

object SchemaRefreshService extends Logging {
  private[this] var refreshCount = 0
  private[this] val activeRefreshes = collection.mutable.HashMap.empty[UUID, Future[Schema]]

  def refreshSchema(db: DatabaseConnection, onSuccess: (Schema) => Unit = s => Unit, onFailure: (Throwable) => Unit = (x) => Unit) = Try {
    SchemaService.get(db.connectionId) match {
      case Some(schema) if activeRefreshes.isDefinedAt(db.connectionId) =>
        log.info(s"Awaiting current refresh of schema [${schema.connectionId}]: $refreshCount.")
        activeRefreshes(db.connectionId).map { s =>
          onSuccess(s)
          s
        }
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
          SchemaService.set(db.connectionId, schema)
          log.info(s"Schema update complete for [${schema.schemaName.getOrElse(schema.connectionId)}] in [${System.currentTimeMillis - startMs}ms].")
          log.info(s"Processed details for [${schema.tables.size}] tables.")
          activeRefreshes.remove(db.connectionId)
          onSuccess(schema)
        }

        val f = DatabaseWorkerPool.submitWork(work _, onSuccessMapped, onFailure)
        activeRefreshes(db.connectionId) = f
        f
      case None => throw new IllegalStateException(s"Attempted to refresh schema [$db.connectionId], which is not loaded.")
    }
  }
}
