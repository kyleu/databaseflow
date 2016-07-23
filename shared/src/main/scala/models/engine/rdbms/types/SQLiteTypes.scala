/* Generated Code */
package models.engine.rdbms.types

import models.engine.DatabaseEngine

trait SQLiteTypes extends DatabaseEngine {
  override val columnTypes = Seq(
    "integer",
    "tinyint",
    "smallint",
    "integer",
    "bigint",
    "float",
    "real",
    "double",
    "numeric",
    "decimal",
    "char",
    "varchar",
    "longvarchar",
    "date",
    "time",
    "timestamp",
    "blob",
    "blob",
    "blob",
    "null",
    "blob",
    "clob",
    "integer"
  )
}
