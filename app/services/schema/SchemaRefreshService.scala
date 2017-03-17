package services.schema

import models.schema.Schema
import services.database.{DatabaseConnection, DatabaseWorkerPool}
import utils.Logging

import scala.util.Try

object SchemaRefreshService extends Logging {
  private[this] var refreshCount = 0

  def refreshSchema(db: DatabaseConnection, onSuccess: (Schema) => Unit = (s) => Unit, onFailure: (Throwable) => Unit = (x) => Unit) = Try {
    SchemaService.get(db.connectionId) match {
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
          onSuccess(schema)
        }

        DatabaseWorkerPool.submitWork(work, onSuccessMapped, onFailure)
      case None => throw new IllegalStateException(s"Attempted to refresh schema [$db.connectionId], which is not loaded.")
    }
  }
}
