package models.engine.rdbms

import models.engine.DatabaseEngine

object MySql {
  val engine = DatabaseEngine(
    id = "mysql",
    name = "MySQL",
    className = "com.mysql.jdbc.Driver",
    exampleUrl = "jdbc:mysql://localhost/test"
  )
}
