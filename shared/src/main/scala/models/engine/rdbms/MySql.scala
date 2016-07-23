package models.engine.rdbms

import models.engine.DatabaseEngine
import models.engine.rdbms.functions.MySQLFunctions
import models.engine.rdbms.types.MySQLTypes

object MySQL extends DatabaseEngine(
  id = "mysql",
  name = "MySQL",
  driverClass = "com.mysql.jdbc.Driver",
  defaultPort = 3306,
  exampleUrl = "jdbc:mysql://localhost/test"
) with MySQLTypes with MySQLFunctions {
  override val leftQuoteIdentifier = "`"
  override val rightQuoteIdentifier = "`"
  override val explain = Some((sql: String) => { "explain format=json " + sql })

  override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
    s"jdbc:mysql://${host.getOrElse("localhost")}:${port.getOrElse(3306)}/${dbName.getOrElse("db")}"
  }
}
