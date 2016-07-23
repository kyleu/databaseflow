package models.engine.rdbms

import models.engine.DatabaseEngine
import models.engine.rdbms.functions.H2Functions
import models.engine.rdbms.types.H2Types

object H2 extends DatabaseEngine(
  id = "h2",
  name = "H2",
  driverClass = "org.h2.Driver",
  defaultPort = 0,
  exampleUrl = "jdbc:h2:~/database.h2db"
) with H2Types with H2Functions {
  override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
    dbName.map(n => s"jdbc:h2:$n").getOrElse(exampleUrl)
  }
}
