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

  override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
    s"jdbc:sqlserver://${host.getOrElse("localhost")}:${port.getOrElse(1433)};databaseName=${dbName.getOrElse("db")}"
  }
}
