package services.connection

import java.sql.SQLSyntaxErrorException
import java.util.UUID

import models.query.QueryError
import models.{ QueryErrorResponse, ResponseMessage, ServerError }
import org.postgresql.util.PSQLException
import utils.DateUtils

import scala.util.control.NonFatal

trait SqlHelper { this: ConnectionService =>
  def sqlCatch(queryId: UUID, sql: String, startMs: Long)(f: () => ResponseMessage) = try {
    f()
  } catch {
    case t: Throwable =>
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      t match {
        case sqlEx: PSQLException =>
          val e = sqlEx.getServerErrorMessage
          QueryErrorResponse(id, QueryError(queryId, sql, e.getSQLState, e.getMessage, Some(e.getLine), Some(e.getPosition), startMs), durationMs)
        case sqlEx: SQLSyntaxErrorException =>
          QueryErrorResponse(id, QueryError(queryId, sql, sqlEx.getSQLState, sqlEx.getMessage, occurred = startMs), durationMs)
        case NonFatal(x) =>
          log.warn(s"Unhandled error running sql [$sql].", x)
          ServerError(x.getClass.getSimpleName, x.getMessage)
        case _ => throw t
      }
  }
}
