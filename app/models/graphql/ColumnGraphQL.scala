package models.graphql

import models.schema.Column

object ColumnGraphQL {
  def getColumnField(col: Column) = if (col.notNull) {
    ColumnNotNullGraphQL.getColumnField(col)
  } else {
    ColumnNullableGraphQL.getColumnField(col)
  }
}
