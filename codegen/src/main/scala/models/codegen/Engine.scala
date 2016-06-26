package models.codegen

import enumeratum._

sealed abstract class Engine(val id: String, val name: String, val driverClass: String, val exampleUrl: String) extends EnumEntry {
  override def toString = id
}

object Engine extends Enum[Engine] {
  case object DB2 extends Engine(
    id = "db2",
    name = "DB2",
    driverClass = "com.ibm.db2.jcc.DB2Driver",
    exampleUrl = "jdbc:db2://hostname:50000/database"
  )
  case object H2 extends Engine(
    id = "h2",
    name = "H2",
    driverClass = "org.h2.Driver",
    exampleUrl = "jdbc:h2:~/database.h2db"
  )
  case object Informix extends Engine(
    id = "informix",
    name = "Informix",
    driverClass = "com.informix.jdbc.IfxDriver",
    exampleUrl = "jdbc:informix-sqli://hostname:1533/database:INFORMIXSERVER=hostname;"
  )
  case object MySQL extends Engine(
    id = "mysql",
    name = "MySQL",
    driverClass = "com.mysql.jdbc.Driver",
    exampleUrl = "jdbc:mysql://localhost/test"
  )
  case object Oracle extends Engine(
    id = "oracle",
    name = "Oracle",
    driverClass = "oracle.jdbc.driver.OracleDriver",
    exampleUrl = "jdbc:oracle:thin:@//hostname:1521/XE"
  )
  case object PostgreSQL extends Engine(
    id = "postgres",
    name = "PostgreSQL",
    driverClass = "org.postgresql.Driver",
    exampleUrl = "jdbc:postgresql://hostname/dbname"
  )
  case object SqlServer extends Engine(
    id = "sqlserver",
    name = "SQL Server",
    driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver",
    exampleUrl = "jdbc:sqlserver://hostname:1433;databaseName=dbname"
  )

  override val values = findValues
}
