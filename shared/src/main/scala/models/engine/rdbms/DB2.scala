package models.engine.rdbms

import models.engine.DatabaseEngine
import models.engine.rdbms.functions.DB2Functions
import models.engine.rdbms.types.DB2Types

object DB2 extends DatabaseEngine(
  id = "db2",
  name = "DB2",
  driverClass = "com.ibm.db2.jcc.DB2Driver",
  defaultPort = Some(50000),
  exampleUrl = "jdbc:db2://hostname:50000/database"
) with DB2Types with DB2Functions {
  override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
    s"jdbc:db2://${host.getOrElse("localhost")}:${port.getOrElse(50000)}/${dbName.getOrElse("db")}"
  }
}
