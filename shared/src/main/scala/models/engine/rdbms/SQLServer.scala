package models.engine.rdbms

import models.engine.DatabaseEngine
import models.engine.rdbms.functions.SQLServerFunctions
import models.engine.rdbms.types.SQLServerTypes

object SQLServer extends DatabaseEngine(
  id = "sqlserver",
  name = "SQL Server",
  driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver",
  defaultPort = 1433,
  exampleUrl = "jdbc:sqlserver://hostname:1433;databaseName=dbname"
) with SQLServerTypes with SQLServerFunctions {
  override val leftQuoteIdentifier = "["
  override val rightQuoteIdentifier = "]"

  override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
    s"jdbc:sqlserver://${host.getOrElse("localhost")}:${port.getOrElse(1433)};databaseName=${dbName.getOrElse("db")}"
  }
}
