package services.query

import java.util.UUID

import akka.actor.ActorRef
import models.{ QueryCheckResponse, ResponseMessage }
import org.h2.jdbc.JdbcSQLException
import services.database.{ DatabaseWorkerPool, MasterDatabase }
import utils.{ ExceptionUtils, Logging }

object QueryCheckService extends Logging {
  def handleCheckQuery(connectionId: UUID, queryId: UUID, sql: String, out: ActorRef) = {
    def work() = {
      log.info(s"Checking query [$queryId] sql [$sql].")
      val db = MasterDatabase.db(connectionId)
      db.withConnection { conn =>
        try {
          val stmt = conn.prepareStatement(sql)
          stmt.getMetaData
          QueryCheckResponse(queryId)
        } catch {
          case x: JdbcSQLException => QueryCheckResponse(queryId, error = Some(x.getMessage))
          case t: Throwable => QueryCheckResponse(queryId, error = Some(t.getClass.getSimpleName + ": " + t.getMessage))
        }
      }
    }
    def onSuccess(rm: ResponseMessage) = out ! rm
    def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "CheckSqlError", t)
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }
}
