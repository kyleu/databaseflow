package models.graphql

import models.schema.{ForeignKey, Table}

object ForeignKeyGraphQL {
  def getForeignKeyField(schema: models.schema.Schema, table: Table, fk: ForeignKey) = {
    val firstRef = fk.references.headOption.getOrElse(throw new IllegalStateException("No references!"))
    val col = table.columns.find(_.name.equalsIgnoreCase(firstRef.source)).getOrElse(throw new IllegalStateException(s"Missing column [${firstRef.source}]."))
    ColumnGraphQL.getColumnField(CommonGraphQL.cleanName(fk.name), col.name, col.description, col.columnType, col.notNull)
  }
}
