package models.graphql

import models.schema.ColumnType
import util.StringKeyUtils

object ColumnGraphQL {
  def getColumnField(
    colName: String, description: Option[String], columnType: ColumnType, notNull: Boolean, sqlTypeName: String
  ) = if (notNull) {
    ColumnNotNullGraphQL.getColumnField(colName, description, columnType, StringKeyUtils.cleanName(colName), sqlTypeName)
  } else {
    ColumnNullableGraphQL.getColumnField(colName, description, columnType, StringKeyUtils.cleanName(colName), sqlTypeName)
  }
}
