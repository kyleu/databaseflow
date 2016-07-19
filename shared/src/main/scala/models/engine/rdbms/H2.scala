package models.engine.rdbms

import models.engine.DatabaseEngine
import models.engine.rdbms.functions.H2Functions
import models.engine.rdbms.types.H2Types

object H2 extends DatabaseEngine(
  id = "h2",
  name = "H2",
  driverClass = "org.h2.Driver",
  exampleUrl = "jdbc:h2:~/database.h2db"
) with H2Types with H2Functions
