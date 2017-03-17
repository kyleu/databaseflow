package models.graphql

import models.schema.Column

object ColumnGraphQL {
  def getColumnField[T](col: Column) = if (col.notNull) {
    ColumnNotNullGraphQL.getColumnField[T](col)
  } else {
    ColumnNullableGraphQL.getColumnField[T](col)
  }
}
