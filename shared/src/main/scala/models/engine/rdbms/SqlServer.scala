/* Generated Code */
// scalastyle:off
package models.engine.rdbms

import models.engine.DatabaseEngine

object SqlServer extends DatabaseEngine(
  id = "sqlserver",
  name = "Microsoft SQL Server",
  driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver",
  exampleUrl = "jdbc:sqlserver://hostname:1433;databaseName=dbname",

  builtInFunctions = Seq(
    "abs",
    "acos",
    "ascii",
    "asin",
    "atan",
    "avg",
    "bit_length",
    "cast",
    "ceiling",
    "char",
    "coalesce",
    "concat",
    "cos",
    "cot",
    "count",
    "current_date",
    "current_time",
    "current_timestamp",
    "datename",
    "day",
    "degrees",
    "exp",
    "extract",
    "floor",
    "getdate",
    "getutcdate",
    "hour",
    "isnull",
    "len",
    "length",
    "locate",
    "log",
    "log10",
    "lower",
    "ltrim",
    "max",
    "min",
    "minute",
    "mod",
    "month",
    "nullif",
    "pi",
    "radians",
    "rand",
    "reverse",
    "round",
    "row_number",
    "rtrim",
    "second",
    "sign",
    "sin",
    "space",
    "sqrt",
    "square",
    "str",
    "substring",
    "sum",
    "tan",
    "trim",
    "upper",
    "user",
    "year"
  ),

  columnTypes = Seq(
    "bigint",
    "binary",
    "bit",
    "varbinary",
    "bit",
    "char",
    "varchar",
    "date",
    "double precision",
    "float",
    "int",
    "nvarchar",
    "varbinary",
    "varchar",
    "nchar",
    "nvarchar",
    "numeric",
    "nvarchar",
    "real",
    "smallint",
    "time",
    "datetime2",
    "smallint",
    "varbinary",
    "varchar"
  )
) {
  override val varchar = "varchar"
  override val quoteIdentifier = "?"
  override val explainSupported = false
  override val analyzeSupported = false
  override val showCreateSupported = false
}
// scalastyle:on
