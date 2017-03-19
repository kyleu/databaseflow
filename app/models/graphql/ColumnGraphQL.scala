package models.graphql

import models.schema.Column

object ColumnGraphQL {
  def cleanName(s: String) = {
    val ret = s.replaceAllLiterally(" ", "_")
    if (ret.head.isDigit) { "_" + ret } else { ret }
  }

  def getColumnField(col: Column) = if (col.notNull) {
    ColumnNotNullGraphQL.getColumnField(col, cleanName(col.name))
  } else {
    ColumnNullableGraphQL.getColumnField(col, cleanName(col.name))
  }
}
