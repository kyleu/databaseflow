package models.engine.rdbms

import models.engine.DatabaseEngine
import models.engine.rdbms.functions.MySQLFunctions
import models.engine.rdbms.types.MySQLTypes

object MySQL extends DatabaseEngine(
  id = "mysql",
  name = "MySQL",
  driverClass = "com.mysql.jdbc.Driver",
  exampleUrl = "jdbc:mysql://localhost/test"
) with MySQLTypes with MySQLFunctions {
  override val leftQuoteIdentifier = "`"
  override val rightQuoteIdentifier = "`"
  override val explain = Some((sql: String) => { "explain format=json " + sql })
}
