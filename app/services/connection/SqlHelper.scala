package services.connection

import java.sql.SQLSyntaxErrorException
import java.util.UUID

import models.query.QueryError
import models.{ QueryErrorResponse, ServerError }
import org.postgresql.util.PSQLException
import utils.DateUtils

import scala.util.control.NonFatal

trait SqlHelper { this: ConnectionService =>
  def sqlCatch(queryId: UUID, sql: String, startMs: Long)(f: () => Unit) = try {
    f()
  } catch {
    case t: Throwable =>
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      t match {
        case sqlEx: PSQLException =>
          val e = sqlEx.getServerErrorMessage
          out ! QueryErrorResponse(id, QueryError(queryId, sql, e.getSQLState, e.getMessage, Some(e.getLine), Some(e.getPosition)), durationMs)
        case sqlEx: SQLSyntaxErrorException =>
          out ! QueryErrorResponse(id, QueryError(queryId, sql, sqlEx.getSQLState, sqlEx.getMessage), durationMs)
        case NonFatal(x) =>
          log.warn(s"Unhandled error running sql [$sql].", x)
          out ! ServerError(x.getClass.getSimpleName, x.getMessage)
        case _ => throw t
      }
  }
}
