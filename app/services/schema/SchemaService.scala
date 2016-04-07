package services.schema

import java.sql.Connection
import java.util.UUID

import akka.actor.ActorRef
import models.{ ProcedureResultResponse, TableResultResponse, ViewResultResponse }
import models.schema.{ Procedure, Schema, Table }
import services.database.DatabaseConnection

object SchemaService {
  case class ConnectionDetails(
    var schema: Option[Schema] = None,
    var tables: Map[String, Table] = Map.empty,
    var views: Map[String, Table] = Map.empty,
    var procedures: Map[String, Procedure] = Map.empty
  )

  private[this] var connectionDetailsMap: Map[UUID, ConnectionDetails] = Map.empty

  def getConnectionDetails(connectionId: UUID) = connectionDetailsMap.getOrElse(connectionId, {
    val ret = ConnectionDetails()
    connectionDetailsMap = connectionDetailsMap + (connectionId -> ret)
    ret
  })
  def getSchema(connectionId: UUID, db: DatabaseConnection, forceRefresh: Boolean = false) = getConnectionDetails(connectionId).schema match {
    case Some(schema) if !forceRefresh => schema
    case None => calculateSchema(connectionId, db)
  }
  def getTable(connectionId: UUID, name: String) = connectionDetailsMap.get(connectionId).flatMap(_.tables.get(name))
  def getView(connectionId: UUID, name: String) = connectionDetailsMap.get(connectionId).flatMap(_.views.get(name))
  def getProcedure(connectionId: UUID, name: String) = connectionDetailsMap.get(connectionId).flatMap(_.procedures.get(name))

  private[this] def withConnection[T](db: DatabaseConnection)(f: (Connection) => T) = {
    val conn = db.source.getConnection()
    try {
      f(conn)
    } finally {
      conn.close()
    }
  }

  private[this] def calculateSchema(connectionId: UUID, db: DatabaseConnection) = withConnection(db) { conn =>
    val catalogName = Option(conn.getCatalog)
    val schemaName = try {
      Option(conn.getSchema)
    } catch {
      case _: AbstractMethodError => None
    }
    val metadata = conn.getMetaData

    val schemaModel = Schema(
      connectionId = connectionId,
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

    val tables = MetadataTables.getTableNames(metadata, catalogName, schemaName, "TABLE")
    val views = MetadataTables.getTableNames(metadata, catalogName, schemaName, "VIEW")
    val procedures = MetadataProcedures.getProcedureNames(metadata, catalogName, schemaName)

    val schema = schemaModel.copy(
      tables = tables,
      views = views,
      procedures = procedures
    )

    getConnectionDetails(connectionId).schema = Some(schema)

    schema
  }

  def getMissingTableDetails(connectionId: UUID, db: DatabaseConnection, notify: Option[ActorRef]) = {
    val metadata = getConnectionDetails(connectionId)
    getSchema(connectionId, db).tables.flatMap { name =>
      if (metadata.tables.get(name).isEmpty) {
        Some(getTableDetail(connectionId, db, name))
      } else {
        None
      }
    }
  }

  def getMissingViewDetails(connectionId: UUID, db: DatabaseConnection, notify: Option[ActorRef]) = {
    val metadata = getConnectionDetails(connectionId)
    getSchema(connectionId, db).views.flatMap { name =>
      if (metadata.views.get(name).isEmpty) {
        Some(getViewDetail(connectionId, db, name))
      } else {
        None
      }
    }
  }

  def getMissingProcedureDetails(connectionId: UUID, db: DatabaseConnection, notify: Option[ActorRef]) = {
    val metadata = getConnectionDetails(connectionId)
    getSchema(connectionId, db).procedures.flatMap { name =>
      if (metadata.procedures.get(name).isEmpty) {
        Some(getProcedureDetail(connectionId, db, name))
      } else {
        None
      }
    }
  }

  def getTableDetail(connectionId: UUID, db: DatabaseConnection, name: String, forceRefresh: Boolean = false) = {
    val metadata = getConnectionDetails(connectionId)
    val sch = getSchema(connectionId, db)
    metadata.tables.get(name) match {
      case Some(p) if !forceRefresh => p
      case _ => withConnection(db) { conn =>
        val ret = MetadataTables.getTableDetails(db, conn, conn.getMetaData, sch.catalog, sch.schemaName, name)
        metadata.tables = metadata.tables + (name -> ret)
        ret
      }
    }
  }

  def getViewDetail(connectionId: UUID, db: DatabaseConnection, name: String, forceRefresh: Boolean = false) = {
    val metadata = getConnectionDetails(connectionId)
    val sch = getSchema(connectionId, db)
    metadata.views.get(name) match {
      case Some(p) if !forceRefresh => p
      case _ => withConnection(db) { conn =>
        val ret = MetadataTables.getViewDetails(db, conn, conn.getMetaData, sch.catalog, sch.schemaName, name)
        metadata.views = metadata.views + (name -> ret)
        ret
      }
    }
  }

  def getProcedureDetail(connectionId: UUID, db: DatabaseConnection, name: String, forceRefresh: Boolean = false) = {
    val metadata = getConnectionDetails(connectionId)
    val sch = getSchema(connectionId, db)
    metadata.procedures.get(name) match {
      case Some(p) if !forceRefresh => p
      case _ => withConnection(db) { conn =>
        val ret = MetadataProcedures.getProcedureDetails(conn.getMetaData, sch.catalog, sch.schemaName, name)
        metadata.procedures = metadata.procedures + (name -> ret)
        ret
      }
    }
  }
}
