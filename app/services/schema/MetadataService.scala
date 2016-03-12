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
      val columns = MetadataColumns.getColumns(metadata, t)
      val indices = MetadataIndices.getIndices(metadata, t)
      t.copy(
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
    val name = schema.orElse(catalog).getOrElse("Unnamed")
    val engine = metadata.getDatabaseProductName
    val engineVersion = metadata.getDatabaseProductVersion
    val driver = metadata.getDriverName
    val driverVersion = metadata.getDriverVersion
    val schemaTerm = metadata.getSchemaTerm

    Schema(
      name = name,
      engine = engine,
      engineVersion = engineVersion,
      driver = driver,
      driverVersion = driverVersion,
      schemaTerm = schemaTerm
    )
  }
}
