package services.schema

import java.sql.Connection
import java.util.UUID

import models.schema.Schema
import services.database.Database

object SchemaService {
  private[this] var schemas = Map.empty[UUID, Schema]

  private[this] def withConnection[T](db: Database)(f: (Connection) => T) = {
    val conn = db.source.getConnection()
    try {
      f(conn)
    } finally {
      conn.close()
    }
  }

  def getSchema(connectionId: UUID, db: Database) = withConnection(db) { conn =>
    val catalog = Option(conn.getCatalog)
    val schema = try {
      Option(conn.getSchema)
    } catch {
      case _: AbstractMethodError => None
    }
    val metadata = conn.getMetaData

    val schemaModel = Schema(
      connectionId = connectionId,
      schemaName = schema,
      catalog = catalog,
      url = metadata.getURL,
      username = metadata.getUserName,
      engine = metadata.getDatabaseProductName,
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

    val tables = MetadataTables.getTableNames(metadata, catalog, schema, "TABLE")
    val views = MetadataTables.getTableNames(metadata, catalog, schema, "VIEW")
    val procedures = MetadataProcedures.getProcedureNames(metadata, catalog, schema)

    val ret = schemaModel.copy(
      tables = tables,
      views = views,
      procedures = procedures
    )

    schemas = schemas + (ret.connectionId -> ret)

    ret
  }

  def getTableDetail(connectionId: UUID, db: Database, name: String) = {
    val schema = schemas.getOrElse(connectionId, getSchema(connectionId, db))
    withConnection(db) { conn =>
      val metadata = conn.getMetaData
      MetadataTables.getTableDetails(metadata, schema.catalog, schema.schemaName, name)
    }
  }

  def getViewDetail(connectionId: UUID, db: Database, name: String) = {
    val schema = schemas.getOrElse(connectionId, getSchema(connectionId, db))
    withConnection(db) { conn =>
      val metadata = conn.getMetaData
      MetadataTables.getTableDetails(metadata, schema.catalog, schema.schemaName, name)
    }
  }

  def getProcedureDetail(connectionId: UUID, db: Database, name: String) = {
    val schema = schemas.getOrElse(connectionId, getSchema(connectionId, db))
    withConnection(db) { conn =>
      val metadata = conn.getMetaData
      MetadataProcedures.getProcedureDetails(metadata, schema.catalog, schema.schemaName, name)
    }
  }
}
