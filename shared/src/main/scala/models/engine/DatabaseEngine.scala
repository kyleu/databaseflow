package models.engine

import models.engine.capabilities._
import enumeratum._

object DatabaseEngine extends Enum[DatabaseEngine] {
  case object H2 extends DatabaseEngine("h2", "H2", "org.h2.Driver", None, H2Capabilities) {
    override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
      dbName.map(n => s"jdbc:h2:$n").getOrElse(cap.exampleUrl)
    }
  }

  case object MySQL extends DatabaseEngine("mysql", "MySQL", "com.mysql.jdbc.Driver", Some(3306), MySQLCapabilities) {
    override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
      s"jdbc:mysql://${host.getOrElse("localhost")}:${port.getOrElse(3306)}/${dbName.getOrElse("db")}"
    }
  }

  case object Oracle extends DatabaseEngine("oracle", "Oracle", "oracle.jdbc.driver.OracleDriver", Some(1521), OracleCapabilities) {
    override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
      s"jdbc:oracle:thin:@//${host.getOrElse("localhost")}:${port.getOrElse(1521)}/${dbName.getOrElse("XE")}"
    }
  }

  case object PostgreSQL extends DatabaseEngine("postgres", "PostgreSQL", "org.postgresql.Driver", Some(5432), PostgreSQLCapabilities) {
    override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
      s"jdbc:postgresql://${host.getOrElse("localhost")}:${port.getOrElse(5432)}/${dbName.getOrElse("db")}"
    }
  }

  case object SQLite extends DatabaseEngine("sqlite", "SQLite", "org.sqlite.JDBC", None, SQLiteCapabilities) {
    override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
      s"jdbc:sqlite:${dbName.getOrElse("db")}"
    }
  }

  case object SQLServer extends DatabaseEngine("sqlserver", "SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver", Some(1433), SQLServerCapabilities) {
    override def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = {
      s"jdbc:sqlserver://${host.getOrElse("localhost")}:${port.getOrElse(1433)};databaseName=${dbName.getOrElse("db")}"
    }
  }

  override val values = findValues
}

sealed abstract class DatabaseEngine(
    val id: String, val name: String, val driverClass: String, val defaultPort: Option[Int], val cap: EngineCapabilities
) extends EnumEntry {
  def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = dbName match {
    case Some(d) => throw new IllegalStateException(s"Cannot form url for provided [$host:$port/$dbName:$extra].")
    case None => cap.exampleUrl
  }
  override def toString = id
}
