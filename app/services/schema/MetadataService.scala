package services.schema

import java.sql.DatabaseMetaData
import javax.sql.DataSource

import models.schema.Schema

object MetadataService {
  def getMetadata(source: DataSource) = {
    val conn = source.getConnection()

    val catalog = Option(conn.getCatalog)
    val schema = try {
      Option(conn.getSchema)
    } catch {
      case _: AbstractMethodError => None
    }
    val metadata = conn.getMetaData

    val schemaModel = getSchema(metadata, catalog, schema)

    val tables = MetadataTables.getTables(metadata, catalog, schema)

    val tablesWithChildren = tables.map { t =>
      val primaryKey = MetadataKeys.getPrimaryKey(metadata, t)
      val columns = MetadataColumns.getColumns(metadata, t)
      val indices = MetadataIndices.getIndices(metadata, t)
      t.copy(
        primaryKey = primaryKey,
        columns = columns,
        indices = indices
      )
    }

    val procedures = MetadataProcedures.getProcedures(metadata, catalog, schema)

    conn.close()

    schemaModel.copy(
      tables = tablesWithChildren,
      procedures = procedures
    )
  }

  def getSchema(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    Schema(
      name = schema.orElse(catalog).getOrElse("Unnamed"),
      url = metadata.getURL,
      username = metadata.getUserName,
      engine = metadata.getDatabaseProductName,
      engineVersion = metadata.getDatabaseProductVersion,
      driver = metadata.getDriverName,
      driverVersion = metadata.getDriverVersion,
      schemaTerm = metadata.getSchemaTerm,
      maxSqlLength = metadata.getMaxStatementLength
    )
  }
}
