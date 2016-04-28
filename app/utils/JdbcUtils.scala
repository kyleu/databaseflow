package utils

import java.sql.{ ResultSet, Timestamp }

import models.database.Row
import org.joda.time.LocalDateTime
import org.postgresql.jdbc.PgArray

object JdbcUtils {
  def toLocalDateTime(row: Row, column: String) = {
    val ts = row.as[Timestamp](column)
    new LocalDateTime(ts.getTime)
  }

  @SuppressWarnings(Array("AsInstanceOf"))
  def toSeq[T](row: Row, column: String): Seq[Any] = {
    val a = row.as[PgArray](column)
    a.getArray.asInstanceOf[Array[T]].toSeq
  }
}
