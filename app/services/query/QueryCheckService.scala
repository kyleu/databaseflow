package services.query

import java.util.UUID

import akka.actor.ActorRef
import com.microsoft.sqlserver.jdbc.SQLServerException
import models.{QueryCheckResponse, ResponseMessage}
import org.h2.jdbc.JdbcSQLException
import org.postgresql.util.PSQLException
import services.database.{DatabaseWorkerPool, DatabaseRegistry}
import utils.{ExceptionUtils, Logging}

object QueryCheckService extends Logging {
  @SuppressWarnings(Array("CatchThrowable"))
  def handleCheckQuery(connectionId: UUID, queryId: UUID, sql: String, out: ActorRef) = {
    def work() = {
      //log.info(s"Checking query [$queryId] sql [$sql].")
      val db = DatabaseRegistry.db(connectionId)
      db.withConnection { conn =>
        try {
          val stmt = conn.prepareStatement(sql)
          stmt.getMetaData
          QueryCheckResponse(queryId)
        } catch {
          case x: JdbcSQLException => QueryCheckResponse(queryId, error = Some(x.getMessage))
          case x: SQLServerException => QueryCheckResponse(queryId, error = Some(x.getMessage))
          case x: PSQLException =>
            val err = x.getServerErrorMessage
            val (line, position) = positionToLineAndPosition(sql, err.getPosition)
            QueryCheckResponse(queryId, error = Some(x.getMessage), line = Some(line), position = Some(position))
          case t: Throwable => QueryCheckResponse(queryId, error = Some(t.getClass.getSimpleName + ": " + t.getMessage))
        }
      }
    }
    def onSuccess(rm: ResponseMessage) = out ! rm
    def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "CheckSqlError", t)
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }

  private[this] def positionToLineAndPosition(sql: String, position: Int) = {
    val lines = scala.io.Source.fromString(sql).getLines()
    1 -> position
  }
}
