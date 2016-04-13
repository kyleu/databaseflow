/* Generated Code */
// scalastyle:off
package models.engine.rdbms

import models.engine.DatabaseEngine

object Informix extends DatabaseEngine(
  id = "informix",
  name = "Informix",
  driverClass = "com.informix.jdbc.IfxDriver",
  exampleUrl = "jdbc:informix-sqli://hostname:1533/database:INFORMIXSERVER=hostname;",

  builtInFunctions = Seq(
    "abs",
    "avg",
    "bit_length",
    "cast",
    "coalesce",
    "concat",
    "count",
    "day",
    "extract",
    "hour",
    "length",
    "locate",
    "lower",
    "max",
    "min",
    "minute",
    "mod",
    "month",
    "nullif",
    "second",
    "sqrt",
    "str",
    "substring",
    "sum",
    "trim",
    "upper",
    "year"
  ),

  columnTypes = Seq(
    "int8",
    "byte",
    "smallint",
    "blob",
    "boolean",
    "char",
    "clob",
    "date",
    "decimal",
    "float",
    "smallfloat",
    "integer",
    "nvarchar",
    "blob",
    "clob",
    "nchar",
    "nclob",
    "decimal",
    "nvarchar",
    "smallfloat",
    "smallint",
    "datetime hour to second",
    "datetime year to fraction",
    "smallint",
    "byte",
    "varchar"
  )
) {
  override val leftQuoteIdentifier = "\""
  override val rightQuoteIdentifier = "\""
}
// scalastyle:on
