package services.schema

import microsoft.sql.DateTimeOffset
import models.database.{Query, Queryable, Row}
import models.engine.DatabaseEngine

object MetadataTimezone {
  private[this] def getSql(engine: DatabaseEngine) = engine match {
    case DatabaseEngine.PostgreSQL => Some("select extract(timezone from now()) / 3600.0 as tz")
    case DatabaseEngine.MySQL => Some("select timestampdiff(second, utc_timestamp, now()) / 3600 as tz")
    case DatabaseEngine.Oracle => Some("select tz_offset(sessiontimezone) as tz from dual")
    case DatabaseEngine.SQLServer => Some("select sysdatetimeoffset() as tz")
    case _ => None
  }

  def getTimezone(q: Queryable, engine: DatabaseEngine) = {
    getSql(engine) match {
      case Some(s) => q.query(new Query[Double] {
        override val sql = s
        override def reduce(rows: Iterator[Row]) = rows.next.as[Any]("tz") match {
          case d: Double => d
          case bd: java.math.BigDecimal => bd.doubleValue
          case dto: DateTimeOffset => dto.getMinutesOffset.toDouble / 60
          case s: String => s.replaceAllLiterally(":", ".").toDouble
          case x => throw new IllegalStateException(s"Encountered unexpected [${x.getClass.getSimpleName}:$x] from timezone query.")
        }
      })
      case None => 0.0
    }
  }
}
