package models.engine.rdbms

import models.engine.DatabaseEngine
import models.engine.rdbms.functions.PostgreSQLFunctions
import models.engine.rdbms.types.PostgreSQLTypes

object PostgreSQL extends DatabaseEngine(
  id = "postgres",
  name = "PostgreSQL",
  driverClass = "org.postgresql.Driver",
  defaultPort = 5432,
  exampleUrl = "jdbc:postgresql://hostname/dbname"
) with PostgreSQLTypes with PostgreSQLFunctions {
  override val explain = Some((sql: String) => { "explain (costs, verbose, format json) " + sql })
  override val analyze = Some((sql: String) => { "explain (analyze, costs, verbose, buffers, format json) " + sql })

  override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
    s"jdbc:postgresql://${host.getOrElse("localhost")}:${port.getOrElse(5432)}/${dbName.getOrElse("db")}"
  }
}
