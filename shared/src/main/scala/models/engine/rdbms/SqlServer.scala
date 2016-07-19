package models.engine.rdbms

import models.engine.DatabaseEngine
import models.engine.rdbms.functions.SqlServerFunctions
import models.engine.rdbms.types.SqlServerTypes

object SqlServer extends DatabaseEngine(
  id = "sqlserver",
  name = "SQL Server",
  driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver",
  exampleUrl = "jdbc:sqlserver://hostname:1433;databaseName=dbname"
) with SqlServerTypes with SqlServerFunctions {
  override val leftQuoteIdentifier = "["
  override val rightQuoteIdentifier = "]"
}
