package models.engine.rdbms

import models.engine.DatabaseEngine
import models.engine.rdbms.functions.InformixFunctions
import models.engine.rdbms.types.InformixTypes

object Informix extends DatabaseEngine(
  id = "informix",
  name = "Informix",
  driverClass = "com.informix.jdbc.IfxDriver",
  defaultPort = Some(1533),
  exampleUrl = "jdbc:informix-sqli://hostname:1533/database:INFORMIXSERVER=hostname;"
) with InformixTypes with InformixFunctions {
  override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
    s"jdbc:informix-sqli://${host.getOrElse("localhost")}:${port.getOrElse(1533)}/${dbName.getOrElse("db")}:INFORMIXSERVER=${host.getOrElse("localhost")}"
  }
}
