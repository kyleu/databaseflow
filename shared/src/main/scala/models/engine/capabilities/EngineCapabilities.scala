package models.engine.capabilities

import models.engine.functions._
import models.engine.types._

sealed abstract class EngineCapabilities(
    val functions: Seq[String] = Nil,
    val columnTypes: Seq[String] = Nil,
    val leftQuote: String = "\"",
    val rightQuote: String = "\"",
    val explain: Option[(String) => String] = None,
    val analyze: Option[(String) => String] = None,
    val exampleUrl: String,
    val transactionsSupported: Boolean = true
)

case object DB2Capabilities extends EngineCapabilities(
  functions = DB2Functions.functions, columnTypes = DB2Types.columnTypes,
  exampleUrl = "jdbc:db2://hostname:50000/database"
)

case object H2Capabilities extends EngineCapabilities(
  functions = H2Functions.functions, columnTypes = H2Types.columnTypes,
  exampleUrl = "jdbc:h2:~/database.h2db"
)

case object InformixCapabilities extends EngineCapabilities(
  functions = InformixFunctions.functions, columnTypes = InformixTypes.columnTypes,
  exampleUrl = "jdbc:informix-sqli://hostname:1533/database"
)

case object MySQLCapabilities extends EngineCapabilities(
  functions = MySQLFunctions.functions, columnTypes = MySQLTypes.columnTypes,
  leftQuote = "`", rightQuote = "`",
  explain = Some((sql: String) => { "explain format=json " + sql }),
  exampleUrl = "jdbc:mysql://localhost/test"
)

case object OracleCapabilities extends EngineCapabilities(
  functions = OracleFunctions.functions, columnTypes = OracleTypes.columnTypes,
  explain = Some((sql: String) => { "explain plan for  " + sql }),
  exampleUrl = "jdbc:oracle:thin:@//hostname:1521/XE"
)

case object PostgreSQLCapabilities extends EngineCapabilities(
  functions = PostgreSQLFunctions.functions :+ "pg_sleep", columnTypes = PostgreSQLTypes.columnTypes,
  explain = Some((sql: String) => { "explain (costs, verbose, format json) " + sql }),
  analyze = Some((sql: String) => { "explain (analyze, costs, verbose, buffers, format json) " + sql }),
  exampleUrl = "jdbc:postgresql://hostname/dbname"
)

case object SQLiteCapabilities extends EngineCapabilities(
  functions = SQLiteFunctions.functions, columnTypes = SQLiteTypes.columnTypes,
  exampleUrl = "jdbc:sqlite:sample.db"
)

case object SQLServerCapabilities extends EngineCapabilities(
  functions = SQLServerFunctions.functions, columnTypes = SQLServerTypes.columnTypes,
  leftQuote = "[", rightQuote = "]",
  exampleUrl = "jdbc:sqlserver://hostname:1433;databaseName=dbname"
)
