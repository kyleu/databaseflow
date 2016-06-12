package utils

import java.sql.{SQLSyntaxErrorException, Timestamp}
import java.util.UUID

import com.mysql.jdbc.exceptions.MySQLStatementCancelledException
import models.database.Row
import models.query.QueryError
import models.{QueryErrorResponse, ResponseMessage}
import org.joda.time.LocalDateTime
import org.postgresql.jdbc.PgArray
import org.postgresql.util.PSQLException

import scala.util.control.NonFatal

object JdbcUtils extends Logging {
  def sqlCatch(queryId: UUID, sql: String, startMs: Long, resultId: UUID)(f: () => ResponseMessage) = try {
    f()
  } catch {
    case NonFatal(t) =>
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      log.warn(s"Encountered query error after [${durationMs}ms] running sql [$sql].", t)
      t match {
        case sqlEx: PSQLException =>
          val e = sqlEx.getServerErrorMessage
          QueryErrorResponse(resultId, QueryError(queryId, sql, e.getSQLState, e.getMessage, Some(e.getLine), Some(e.getPosition), startMs), durationMs)
        case sqlEx: SQLSyntaxErrorException =>
          QueryErrorResponse(resultId, QueryError(queryId, sql, sqlEx.getSQLState, sqlEx.getMessage, occurred = startMs), durationMs)
        case sqlEx: MySQLStatementCancelledException =>
          QueryErrorResponse(resultId, QueryError(queryId, sql, sqlEx.getSQLState, sqlEx.getMessage, occurred = startMs), durationMs)
        case x =>
          QueryErrorResponse(resultId, QueryError(queryId, sql, x.getClass.getSimpleName, x.getMessage, occurred = startMs), durationMs)
      }
  }

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
