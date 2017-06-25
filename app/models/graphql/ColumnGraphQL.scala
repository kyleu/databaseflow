package models.graphql

import models.schema.Column

object ColumnGraphQL {
  def getColumnField(col: Column) = if (col.notNull) {
    ColumnNotNullGraphQL.getColumnField(col, CommonGraphQL.cleanName(col.name))
  } else {
    ColumnNullableGraphQL.getColumnField(col, CommonGraphQL.cleanName(col.name))
  }
}
