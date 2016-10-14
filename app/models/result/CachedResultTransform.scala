package models.result

import models.query.QueryResult
import models.schema.ColumnType

object CachedResultTransform {
  def transform(columns: Seq[QueryResult.Col], data: Seq[Option[Any]]) = columns.zip(data).map {
    case x if x._1.t == ColumnType.DateType && x._2.exists(_.isInstanceOf[String]) => x._2.map(_.toString.stripSuffix(" 00:00:00"))
    case x if x._1.t == ColumnType.StringType && x._2.exists(_.isInstanceOf[String]) => x._2.map { s =>
      val str = s.toString
      if (str.length > x._1.precision.getOrElse(Int.MaxValue)) {
        str.substring(0, x._1.precision.getOrElse(Int.MaxValue))
      } else {
        str
      }
    }
    case x => x._2
  }
}
