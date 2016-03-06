package models.ddl

import java.util.UUID

import models.database.{Row, SingleRowQuery, Statement}

object DdlQueries {
  val testUserId = UUID.fromString("00000000-0000-0000-0000-000000000000")

  final case class DoesTableExist(tableName: String) extends SingleRowQuery[Boolean] {
    override val sql = "select count(*) as c from information_schema.tables WHERE (table_name = ? or table_name = ?);"
    override val values = tableName :: tableName.toUpperCase :: Nil
    override def map(row: Row) = row.as[Long]("c") > 0
  }

  case object DoesTestUserExist extends SingleRowQuery[Boolean] {
    override def sql = s"select count(*) as c from users where id = '$testUserId'"
    override def map(row: Row): Boolean = row.as[Long]("c") == 1L
  }

  case object InsertTestUser extends Statement {
    override def sql = s"""insert into users (
      id, username, prefs, profiles, roles, created
    ) values (
      '$testUserId', 'Test User', '{ }', '', 'user', '2016-01-01 00:00:00.000'
    )"""
  }

  final case class TruncateTable(tableName: String) extends Statement {
    override val sql = s"truncate table $tableName"
  }

  def trim(s: String) = s.replaceAll("""[\s]+""", " ").trim
}
