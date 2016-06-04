package services.schema

import models.schema.Schema
import services.database.DatabaseConnection
import utils.Logging

object SchemaHelper extends Logging {
  def calculateSchema(db: DatabaseConnection) = db.withConnection { conn =>
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

    val tables = MetadataTables.getTables(metadata, catalogName, schemaName)
    val views = MetadataViews.getViews(metadata, catalogName, schemaName)
    val procedures = MetadataProcedures.getProcedures(metadata, catalogName, schemaName)

    val schema = schemaModel.copy(
      tables = tables,
      views = views,
      procedures = procedures
    )

    schema
  }
}
