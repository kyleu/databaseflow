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
    private[this] val (lq, rq) = engine.cap.leftQuote -> engine.cap.rightQuote
    private[this] val stddev = if (engine == DatabaseEngine.SQLite) { "0" } else { s"stddev($lq$name$rq)" }
    override def sql = s"""
      select
        count($lq$name$rq) c,
        count(distinct $lq$name$rq) d,
        min($lq$name$rq) mn,
        max($lq$name$rq) mx,
        sum($lq$name$rq) s,
        avg($lq$name$rq) a,
        variance($lq$name$rq) v,
        $stddev sd
      from $owner
    """.trim
    override def map(row: Row) = ColumnDetails(
      count = row.as[Any]("c").toString.toLong,
      distinctCount = row.as[Any]("d").toString.toLong,
      min = row.asOpt[Any]("mn").map(_.toString.toDouble),
      max = row.asOpt[Any]("mx").map(_.toString.toDouble),
      sum = row.asOpt[Any]("s").map(_.toString.toDouble),
      avg = row.asOpt[Any]("a").map(_.toString.toDouble),
      variance = row.asOpt[Any]("v").map(_.toString.toDouble),
      stdDev = row.asOpt[Any]("sd").map(_.toString.toDouble)
    )
  }
}
