package models.codegen.dialect

import java.sql.{JDBCType, Types}

import models.codegen.Capabilities
import services.codegen.DialectProvider

object SQLiteDialect extends DialectProvider.FlowDialect(None) {
  override def functions() = Seq(
    Capabilities.SqlFunction("concat", "varargs"),
    Capabilities.SqlFunction("mod", "template"),
    Capabilities.SqlFunction("substr", "standard"),
    Capabilities.SqlFunction("substring", "standard")
  )

  override def types() = Seq(
    (Types.BIT, "integer"),
    (Types.TINYINT, "tinyint"),
    (Types.SMALLINT, "smallint"),
    (Types.INTEGER, "integer"),
    (Types.BIGINT, "bigint"),
    (Types.FLOAT, "float"),
    (Types.REAL, "real"),
    (Types.DOUBLE, "double"),
    (Types.NUMERIC, "numeric"),
    (Types.DECIMAL, "decimal"),
    (Types.CHAR, "char"),
    (Types.VARCHAR, "varchar"),
    (Types.LONGVARCHAR, "longvarchar"),
    (Types.DATE, "date"),
    (Types.TIME, "time"),
    (Types.TIMESTAMP, "timestamp"),
    (Types.BINARY, "blob"),
    (Types.VARBINARY, "blob"),
    (Types.LONGVARBINARY, "blob"),
    (Types.NULL, "null"),
    (Types.BLOB, "blob"),
    (Types.CLOB, "clob"),
    (Types.BOOLEAN, "integer")
  ).map {
      case (t, n) => JDBCType.valueOf(t).toString -> Some(n)
    }.toArray
}
