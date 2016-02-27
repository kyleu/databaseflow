package models.engine.rdbms

import models.engine.DatabaseEngine

object H2 {
  val engine = DatabaseEngine(
    id = "h2",
    name = "H2",
    className = "rg.h2.Driver",
    exampleUrl = "jdbc:h2:~/database.h2db"
  )
}
