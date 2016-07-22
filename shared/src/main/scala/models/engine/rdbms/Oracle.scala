package models.engine.rdbms

import models.engine.DatabaseEngine
import models.engine.rdbms.functions.OracleFunctions
import models.engine.rdbms.types.OracleTypes

object Oracle extends DatabaseEngine(
  id = "oracle",
  name = "Oracle",
  driverClass = "oracle.jdbc.driver.OracleDriver",
  exampleUrl = "jdbc:oracle:thin:@//hostname:1521/XE"
) with OracleTypes with OracleFunctions {
  override val explain = Some((sql: String) => { "explain plan for  " + sql })

  override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
    s"jdbc:oracle:thin:@//${host.getOrElse("localhost")}:${port.getOrElse(1521)}/${dbName.getOrElse("XE")}"
  }
}
