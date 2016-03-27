package services.schema

import java.sql.DatabaseMetaData
import javax.sql.DataSource

import models.schema.{ Table, Schema }

object SchemaService {
  def getSchema(source: DataSource) = {
    val conn = source.getConnection()

    val catalog = Option(conn.getCatalog)
    val schema = try {
      Option(conn.getSchema)
    } catch {
      case _: AbstractMethodError => None
    }
    val metadata = conn.getMetaData

    val schemaModel = getSchemaModel(metadata, catalog, schema)

    val tables = MetadataTables.getTableNames(metadata, catalog, schema, "TABLE")
    val views = MetadataTables.getTableNames(metadata, catalog, schema, "VIEW")
    val procedures = MetadataProcedures.getProcedureNames(metadata, catalog, schema)

    conn.close()

    schemaModel.copy(
      tables = tables,
      views = views,
      procedures = procedures
    )
  }

  private[this] def getSchemaModel(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    Schema(
      name = schema.orElse(catalog).getOrElse("Unnamed"),
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
      procedures = Nil,
      clientInfoProperties = Nil
    )
  }
}
