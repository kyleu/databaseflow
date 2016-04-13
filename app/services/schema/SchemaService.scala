package services.schema

import java.util.UUID

import models.schema.Schema
import services.database.DatabaseConnection
import utils.Logging

import scala.util.{ Failure, Success, Try }

object SchemaService extends Logging {
  private[this] var schemaMap: Map[UUID, Schema] = Map.empty

  def getSchema(db: DatabaseConnection, forceRefresh: Boolean = false) = Try {
    schemaMap.get(db.connectionId) match {
      case Some(schema) if forceRefresh =>
        calculateSchema(db)
        refreshSchema(db) match {
          case Success(s) => s
          case Failure(x) => throw x
        }
      case Some(schema) => schema
      case None => calculateSchema(db)
    }
  }

  def refreshSchema(db: DatabaseConnection) = Try {
    schemaMap.get(db.connectionId) match {
      case Some(schema) => db.withConnection { conn =>
        log.info(s"Refreshing schema [${schema.schemaName.getOrElse(schema.connectionId)}].")
        val metadata = conn.getMetaData
        val updated = schema.copy(
          tables = MetadataTables.withTableDetails(db, conn, metadata, schema.tables),
          views = MetadataViews.withViewDetails(db, conn, metadata, schema.views),
          procedures = MetadataProcedures.withProcedureDetails(metadata, schema.catalog, schema.schemaName, schema.procedures),
          detailsLoadedAt = Some(System.currentTimeMillis)
        )
        schemaMap = schemaMap + (db.connectionId -> updated)
        updated
      }
      case None => throw new IllegalStateException(s"Attempted to refresh schema [$db.connectionId], which is not loaded.")
    }
  }

  def getTable(connectionId: UUID, name: String) = schemaMap.get(connectionId).flatMap(_.tables.find(_.name == name))
  def getView(connectionId: UUID, name: String) = schemaMap.get(connectionId).flatMap(_.views.find(_.name == name))
  def getProcedure(connectionId: UUID, name: String) = schemaMap.get(connectionId).flatMap(_.procedures.find(_.name == name))

  private[this] def calculateSchema(db: DatabaseConnection) = db.withConnection { conn =>
    val catalogName = Option(conn.getCatalog)
    val schemaName = try {
      Option(conn.getSchema)
    } catch {
      case _: AbstractMethodError => None
    }
    val metadata = conn.getMetaData

    val schemaModel = Schema(
      connectionId = db.connectionId,
      schemaName = schemaName,
      catalog = catalogName,
      url = metadata.getURL,
      username = metadata.getUserName,
      engine = db.engine.id,
      engineVersion = metadata.getDatabaseProductVersion,
      driver = metadata.getDriverName,
      driverVersion = metadata.getDriverVersion,
      catalogTerm = metadata.getCatalogTerm,
      schemaTerm = metadata.getSchemaTerm,
      procedureTerm = metadata.getProcedureTerm,
      maxSqlLength = metadata.getMaxStatementLength,

      tables = Nil,
      views = Nil,
      procedures = Nil
    )

    val tables = MetadataTables.getTables(db, conn, metadata, catalogName, schemaName)
    val views = MetadataViews.getViews(db, conn, metadata, catalogName, schemaName)
    val procedures = MetadataProcedures.getProcedures(metadata, catalogName, schemaName)

    val schema = schemaModel.copy(
      tables = tables,
      views = views,
      procedures = procedures
    )

    schemaMap = schemaMap + (db.connectionId -> schema)

    schema
  }
}
