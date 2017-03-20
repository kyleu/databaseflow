package models.queries.column

import models.database.{Row, SingleRowQuery}
import models.schema.ColumnDetails

object ColumnDetailQueries {
  case class StringColumnDetail(owner: String, name: String) extends SingleRowQuery[ColumnDetails] {
    override def sql = s"select count($name) c, count(distinct $name) d from $owner"
    override def map(row: Row) = ColumnDetails(
      count = row.as[Long]("c"),
      distinctCount = row.as[Long]("d")
    )
  }

  case class NumberColumnDetail(owner: String, name: String) extends SingleRowQuery[ColumnDetails] {
    override def sql = s"select count($name) c, count(distinct $name) d from $owner"
    override def map(row: Row) = ColumnDetails(
      count = row.as[Long]("c"),
      distinctCount = row.as[Long]("d")
    )
  }
}
