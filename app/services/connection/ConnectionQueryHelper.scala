package services.connection

import java.sql.SQLSyntaxErrorException
import java.util.UUID

import akka.actor.ActorRef
import models.{ ServerError, QueryErrorResponse }
import models.query.QueryError
import org.postgresql.util.PSQLException
import utils.{ Logging, DateUtils }

import scala.util.control.NonFatal

object ConnectionQueryHelper extends Logging {
  def handleSqlException(id: UUID, sql: String, t: Throwable, startMs: Long, out: ActorRef) = t match {
    case sqlEx: PSQLException =>
      val e = sqlEx.getServerErrorMessage
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryErrorResponse(id, QueryError(sql, e.getSQLState, e.getMessage, Some(e.getLine), Some(e.getPosition)), durationMs)
    case sqlEx: SQLSyntaxErrorException =>
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryErrorResponse(id, QueryError(sql, sqlEx.getSQLState, sqlEx.getMessage), durationMs)
    case NonFatal(x) =>
      log.warn(s"Unhandled error running sql [$sql].", x)
      val error = ServerError(x.getClass.getSimpleName, x.getMessage)
      out ! error
    case _ => throw t
  }
}
