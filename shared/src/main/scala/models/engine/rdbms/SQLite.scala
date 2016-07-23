package models.engine.rdbms

import models.engine.DatabaseEngine
import models.engine.rdbms.functions.SQLiteFunctions
import models.engine.rdbms.types.SQLiteTypes

object SQLite extends DatabaseEngine(
  id = "sqlite",
  name = "SQLite",
  driverClass = "org.sqlite.JDBC",
  defaultPort = 0,
  exampleUrl = "jdbc:sqlite:sample.db"
) with SQLiteTypes with SQLiteFunctions {
  override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
    s"jdbc:sqlite:${dbName.getOrElse("db")}"
  }
}
