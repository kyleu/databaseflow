package models.graphql

import models.schema.ColumnType

object ColumnGraphQL {
  def getColumnField(name: String, colName: String, description: Option[String], columnType: ColumnType, notNull: Boolean) = if (notNull) {
    ColumnNotNullGraphQL.getColumnField(colName, description, columnType, name)
  } else {
    ColumnNullableGraphQL.getColumnField(colName, description, columnType, name)
  }
}
