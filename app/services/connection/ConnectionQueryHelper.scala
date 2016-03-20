package services.connection

import java.sql.SQLSyntaxErrorException
import java.util.UUID

import akka.actor.ActorRef
import models.queries.DynamicQuery
import models.template.QueryPlanTemplate
import models.{ QueryResultResponse, ServerError, QueryErrorResponse }
import models.query.{ QueryResult, QueryError }
import org.postgresql.util.PSQLException
import services.database.Database
import utils.{ Logging, DateUtils }

import scala.util.control.NonFatal

object ConnectionQueryHelper extends Logging {
  def handleRunQuery(db: Database, sql: String, out: ActorRef) = {
    log.info(s"Performing query action [run] for sql [$sql].")
    val id = UUID.randomUUID
    val startMs = DateUtils.nowMillis
    try {
      val result = db.query(DynamicQuery(sql))
      //log.info(s"Query result: [$result].")
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryResultResponse(id, QueryResult(sql, result._1, result._2), durationMs)
    } catch {
      case x: Throwable => ConnectionQueryHelper.handleSqlException(id, sql, x, startMs, out)
    }
  }

  def handleExplainQuery(db: Database, sql: String, out: ActorRef) = {
    if (db.engine.explainSupported) {
      val explainSql = db.engine.explain(sql)
      log.info(s"Performing query action [explain] for sql [$explainSql].")

      val id = UUID.randomUUID
      val startMs = DateUtils.nowMillis
      try {
        val result = db.query(DynamicQuery(explainSql))
        //log.info(s"Query result: [$result].")
        val durationMs = (DateUtils.nowMillis - startMs).toInt
        out ! QueryResultResponse(id, QueryResult(sql, result._1, result._2), durationMs)
      } catch {
        case x: Throwable => ConnectionQueryHelper.handleSqlException(id, sql, x, startMs, out)
      }
    } else {
      out ! ServerError("explain-not-supported", s"Explain is not avaialble for [${db.engine}].")
    }
  }

  def handleAnalyzeQuery(db: Database, sql: String, out: ActorRef) = {
    out ! QueryPlanTemplate.testPlan("analyze")
  }

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
