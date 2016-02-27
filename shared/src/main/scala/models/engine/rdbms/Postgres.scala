package models.engine.rdbms

import models.engine.DatabaseEngine

object Postgres {
  val engine = DatabaseEngine("postgres", "PostgreSQL")
}
