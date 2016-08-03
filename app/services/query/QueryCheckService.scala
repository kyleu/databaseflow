package services.query

import java.util.UUID

import akka.actor.ActorRef
import com.microsoft.sqlserver.jdbc.SQLServerException
import models.query.{QueryCheckResult, SqlParser}
import models.{QueryCheckResponse, ResponseMessage}
import org.h2.jdbc.JdbcSQLException
import org.postgresql.util.PSQLException
import services.database.{DatabaseRegistry, DatabaseWorkerPool}
import utils.{ExceptionUtils, Logging}

object QueryCheckService extends Logging {
  @SuppressWarnings(Array("CatchThrowable"))
  def handleCheckQuery(connectionId: UUID, queryId: UUID, sql: String, out: ActorRef) = {
    def work() = {
      //log.info(s"Checking query [$queryId] sql [$sql].")
      val db = DatabaseRegistry.db(connectionId)
      val results = db.withConnection { conn =>
        SqlParser.split(sql).map { s =>
          try {
            val stmt = conn.prepareStatement(s._1)
            stmt.getMetaData
            QueryCheckResult(s._1, error = None)
          } catch {
            case x: JdbcSQLException => QueryCheckResult(s._1, error = Some(x.getMessage))
            case x: SQLServerException => QueryCheckResult(s._1, error = Some(x.getMessage))
            case x: PSQLException => QueryCheckResult(s._1, error = Some(x.getMessage), index = Some(x.getServerErrorMessage.getPosition))
            case t: Throwable => QueryCheckResult(s._1, error = Some(t.getClass.getSimpleName + ": " + t.getMessage))
          }
        }
      }
      QueryCheckResponse(queryId, results)
    }
    def onSuccess(rm: ResponseMessage) = out ! rm
    def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "CheckSqlError", t)
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }
}
