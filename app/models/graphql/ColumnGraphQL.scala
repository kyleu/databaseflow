package models.graphql

import models.schema.ColumnType

object ColumnGraphQL {
  def getColumnField(
    colName: String, description: Option[String], columnType: ColumnType, notNull: Boolean, sqlTypeName: String
  ) = if (notNull) {
    ColumnNotNullGraphQL.getColumnField(colName, description, columnType, CommonGraphQL.cleanName(colName), sqlTypeName)
  } else {
    ColumnNullableGraphQL.getColumnField(colName, description, columnType, CommonGraphQL.cleanName(colName), sqlTypeName)
  }
}
