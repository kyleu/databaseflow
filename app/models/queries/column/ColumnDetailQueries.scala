package models.queries.column

import models.database.{Row, SingleRowQuery}
import models.engine.DatabaseEngine
import models.schema.ColumnDetails

object ColumnDetailQueries {
  case class BasicColumnDetail(owner: String, name: String) extends SingleRowQuery[ColumnDetails] {
    override def sql = s"select count($name) c, count(distinct $name) d from $owner"
    override def map(row: Row) = ColumnDetails(
      count = row.as[Long]("c"),
      distinctCount = row.as[Long]("d")
    )
  }

  case class NumberColumnDetail(engine: DatabaseEngine, owner: String, name: String) extends SingleRowQuery[ColumnDetails] {
    override def sql = s"""
      select
        count($name) c,
        count(distinct $name) d,
        min($name) mn,
        max($name) mx,
        sum($name) s,
        avg($name) a,
        variance($name) v,
        stddev($name) sd
      from $owner
    """
    override def map(row: Row) = ColumnDetails(
      count = row.as[Long]("c"),
      distinctCount = row.as[Long]("d"),
      min = row.asOpt[Any]("mn").map(_.toString.toDouble),
      max = row.asOpt[Any]("mx").map(_.toString.toDouble),
      sum = row.asOpt[Any]("s").map(_.toString.toDouble),
      avg = row.asOpt[Any]("a").map(_.toString.toDouble),
      variance = row.asOpt[Any]("v").map(_.toString.toDouble),
      stdDev = row.asOpt[Any]("sd").map(_.toString.toDouble)
    )
  }
}
