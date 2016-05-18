package models.ddl

import models.database.{ Row, SingleRowQuery, Statement }

object DdlQueries {
  case class DoesTableExist(tableName: String) extends SingleRowQuery[Boolean] {
    override val sql = "select count(*) as c from information_schema.tables WHERE (table_name = ? or table_name = ?);"
    override val values = tableName :: tableName.toUpperCase :: Nil
    override def map(row: Row) = row.as[Long]("c") > 0
  }

  case class TruncateTable(tableName: String) extends Statement {
    override val sql = s"truncate table $tableName"
  }

  def trim(s: String) = s.replaceAll("""[\s]+""", " ").trim
}
