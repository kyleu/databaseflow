package services.connection

import java.sql.SQLSyntaxErrorException
import java.util.UUID

import models.query.QueryError
import models.{ QueryErrorResponse, ResponseMessage, ServerError }
import org.postgresql.util.PSQLException
import utils.DateUtils

import scala.util.control.NonFatal

trait SqlHelper { this: ConnectionService =>
  def sqlCatch(queryId: UUID, sql: String, startMs: Long, resultId: UUID)(f: () => ResponseMessage) = try {
    f()
  } catch {
    case NonFatal(t) =>
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      t match {
        case sqlEx: PSQLException =>
          val e = sqlEx.getServerErrorMessage
          QueryErrorResponse(resultId, QueryError(queryId, "Query Error", sql, e.getSQLState, e.getMessage, Some(e.getLine), Some(e.getPosition), startMs), durationMs)
        case sqlEx: SQLSyntaxErrorException =>
          QueryErrorResponse(resultId, QueryError(queryId, "Query Error", sql, sqlEx.getSQLState, sqlEx.getMessage, occurred = startMs), durationMs)
        case x =>
          log.warn(s"Unhandled error running sql [$sql].", x)
          ServerError(x.getClass.getSimpleName, x.getMessage)
      }
  }
}
