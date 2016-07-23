package services.schema

import java.sql.DatabaseMetaData

import models.database.Row
import models.queries.QueryTranslations
import models.schema.Column
import utils.NullUtils

object MetadataColumns {
  def getAllColumns(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getColumns(catalog.orNull, schema.orNull, NullUtils.inst, NullUtils.inst)
    val columns = new Row.Iter(rs).map { row =>
      row.as[String]("TABLE_NAME") -> fromRow(row)
    }.toList
    columns.sortBy(_._1).map(_._2)
  }

  def getColumns(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String], name: String) = {
    val rs = metadata.getColumns(catalog.orNull, schema.orNull, name, NullUtils.inst)
    val columns = new Row.Iter(rs).map(fromRow).toList
    columns.sortBy(_._1).map(_._2)
  }

  private[this] def fromRow(row: Row) = {
    val notNull = JdbcHelper.intVal(row.as[Any]("NULLABLE"))
    val colType = JdbcHelper.intVal(row.as[Any]("DATA_TYPE"))
    val colSize = row.asOpt[Any]("COLUMN_SIZE").map(JdbcHelper.intVal)
    val position = JdbcHelper.intVal(row.as[Any]("ORDINAL_POSITION"))
    position -> Column(
      name = row.as[String]("COLUMN_NAME"),
      description = row.asOpt[String]("REMARKS"),
      definition = row.asOpt[String]("COLUMN_DEF"),
      primaryKey = false, //row.as[Boolean]("?"),
      notNull = notNull == 0, // IS_NULLABLE?
      autoIncrement = row.asOpt[String]("IS_AUTOINCREMENT").contains("YES"),
      columnType = QueryTranslations.forType(colType), // SQL_DATA_TYPE? SOURCE_DATA_TYPE?
      sqlTypeCode = colType, // SQL_DATA_TYPE? SOURCE_DATA_TYPE?
      sqlTypeName = row.as[String]("TYPE_NAME"),
      size = row.asOpt[Any]("COLUMN_SIZE").fold("?")(_.toString),
      sizeAsInt = colSize.getOrElse(0), // ?
      scale = 0, // BUFFER_LENGTH? DECIMAL_DIGITS? NUM_PREC_RADIX?
      defaultValue = row.asOpt[String]("COLUMN_DEF")
    )
  }
}
