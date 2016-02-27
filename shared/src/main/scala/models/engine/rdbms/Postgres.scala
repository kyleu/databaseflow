package models.engine.rdbms

import models.engine.DatabaseEngine

object Postgres {
  val engine = DatabaseEngine(
    id = "postgres",
    name = "PostgreSQL",
    className = "org.postgresql.Driver",
    exampleUrl = "jdbc:postgresql://hostname:port/dbname"
  )
}
