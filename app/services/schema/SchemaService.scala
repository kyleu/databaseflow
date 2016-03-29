package services.schema

import java.sql.Connection
import java.util.UUID

import models.schema.{ Procedure, Schema, Table }
import services.database.Database

object SchemaService {
  private[this] var schemas = Map.empty[UUID, Schema]
  private[this] var tables = Map.empty[String, Table]
  private[this] var views = Map.empty[String, Table]
  private[this] var procedures = Map.empty[String, Procedure]

  def getSchema(id: UUID) = schemas.get(id)
  def getTable(name: String) = tables.get(name)
  def getView(name: String) = views.get(name)
  def getProcedure(name: String) = procedures.get(name)

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

  def getTableDetail(connectionId: UUID, db: Database, name: String, forceRefresh: Boolean = false) = {
    val schema = schemas.getOrElse(connectionId, getSchema(connectionId, db))
    tables.get(name) match {
      case Some(p) if !forceRefresh => p
      case _ => withConnection(db) { conn =>
        val ret = MetadataTables.getTableDetails(conn.getMetaData, schema.catalog, schema.schemaName, name)
        tables = tables + (name -> ret)
        ret
      }
    }
  }

  def getViewDetail(connectionId: UUID, db: Database, name: String, forceRefresh: Boolean = false) = {
    val schema = schemas.getOrElse(connectionId, getSchema(connectionId, db))
    views.get(name) match {
      case Some(p) if !forceRefresh => p
      case _ => withConnection(db) { conn =>
        val ret = MetadataTables.getTableDetails(conn.getMetaData, schema.catalog, schema.schemaName, name)
        views = views + (name -> ret)
        ret
      }
    }
  }

  def getProcedureDetail(connectionId: UUID, db: Database, name: String, forceRefresh: Boolean = false) = {
    val schema = schemas.getOrElse(connectionId, getSchema(connectionId, db))
    procedures.get(name) match {
      case Some(p) if !forceRefresh => p
      case _ => withConnection(db) { conn =>
        val ret = MetadataProcedures.getProcedureDetails(conn.getMetaData, schema.catalog, schema.schemaName, name)
        procedures = procedures + (name -> ret)
        ret
      }
    }
  }
}
