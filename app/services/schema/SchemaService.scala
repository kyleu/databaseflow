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

    def fixTable(t: Table) = t.copy(
      columns = MetadataColumns.getColumns(metadata, t),
      rowIdentifier = MetadataIndentifiers.getRowIdentifier(metadata, t),
      primaryKey = MetadataKeys.getPrimaryKey(metadata, t),
      foreignKeys = MetadataKeys.getForeignKeys(metadata, t),
      indexes = MetadataIndexes.getIndexes(metadata, t)
    )

    val tables = MetadataTables.getTables(metadata, catalog, schema, "TABLE")
    val tablesWithChildren = tables.map(fixTable)

    val views = MetadataTables.getTables(metadata, catalog, schema, "VIEW")
    val viewsWithChildren = views.map(fixTable)

    val procedures = MetadataProcedures.getProcedures(metadata, catalog, schema)
    val clientInfoProperties = MetadataClientInfoProperties.getClientInfoProperties(metadata)

    conn.close()

    schemaModel.copy(
      tables = tablesWithChildren,
      views = viewsWithChildren,
      procedures = procedures,
      clientInfoProperties = clientInfoProperties
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
