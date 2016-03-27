package services.connection

import java.sql.SQLSyntaxErrorException
import java.util.UUID

import akka.actor.ActorRef
import models.queries.DynamicQuery
import models.template.QueryPlanTemplate
import models.{ PlanResultResponse, QueryErrorResponse, QueryResultResponse, ServerError }
import models.query.{ QueryError, QueryResult }
import org.postgresql.util.PSQLException
import services.database.Database
import services.plan.PlanParseService
import utils.{ DateUtils, Logging }

import scala.util.control.NonFatal

object ConnectionQueryHelper extends Logging {
  def handleRunQuery(db: Database, queryId: UUID, sql: String, out: ActorRef) = {
    log.info(s"Performing query action [run] for sql [$sql].")
    val id = UUID.randomUUID
    val startMs = DateUtils.nowMillis
    try {
      val result = db.query(DynamicQuery(sql))
      //log.info(s"Query result: [$result].")
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryResultResponse(id, QueryResult(
        queryId = queryId,
        title = "Query Results",
        sql = sql,
        columns = result._1,
        data = result._2,
        sortable = false,
        occurred = startMs
      ), durationMs)
    } catch {
      case x: Throwable => ConnectionQueryHelper.handleSqlException(id, queryId, sql, x, startMs, out)
    }
  }

  def handleExplainQuery(db: Database, queryId: UUID, sql: String, out: ActorRef) = {
    if (db.engine.explainSupported) {
      val explainSql = db.engine.explain(sql)
      log.info(s"Performing query action [explain] for sql [$explainSql].")

      val id = UUID.randomUUID
      val startMs = DateUtils.nowMillis
      try {
        implicit val engine = db.engine
        val result = db.query(DynamicQuery(explainSql))
        //log.info(s"Query result: [$result].")
        val durationMs = (DateUtils.nowMillis - startMs).toInt
        val planResponse = PlanParseService.parse(sql, queryId, PlanParseService.resultPlanString(result))
        out ! PlanResultResponse(id, planResponse, durationMs)
      } catch {
        case x: Throwable => ConnectionQueryHelper.handleSqlException(id, queryId, sql, x, startMs, out)
      }
    } else {
      out ! ServerError("explain-not-supported", s"Explain is not avaialble for [${db.engine}].")
    }
  }

  def handleAnalyzeQuery(db: Database, queryId: UUID, sql: String, out: ActorRef) = {
    out ! QueryPlanTemplate.testPlan("analyze", queryId)
  }

  def handleShowTableData(db: Database, queryId: UUID, name: String, out: ActorRef) = {
    log.info(s"Showing table [$name].")
    val id = UUID.randomUUID
    val startMs = DateUtils.nowMillis
    val sql = s"select * from $name limit 1001"
    try {
      val result = db.query(DynamicQuery(sql))
      //log.info(s"Query result: [$result].")
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryResultResponse(id, QueryResult(
        queryId = queryId,
        title = name,
        sql = sql,
        columns = result._1,
        data = result._2,
        sortable = true,
        occurred = startMs
      ), durationMs)
    } catch {
      case x: Throwable => ConnectionQueryHelper.handleSqlException(id, queryId, sql, x, startMs, out)
    }
  }

  def handleSqlException(id: UUID, queryId: UUID, sql: String, t: Throwable, startMs: Long, out: ActorRef) = t match {
    case sqlEx: PSQLException =>
      val e = sqlEx.getServerErrorMessage
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryErrorResponse(id, QueryError(queryId, sql, e.getSQLState, e.getMessage, Some(e.getLine), Some(e.getPosition)), durationMs)
    case sqlEx: SQLSyntaxErrorException =>
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryErrorResponse(id, QueryError(queryId, sql, sqlEx.getSQLState, sqlEx.getMessage), durationMs)
    case NonFatal(x) =>
      log.warn(s"Unhandled error running sql [$sql].", x)
      val error = ServerError(x.getClass.getSimpleName, x.getMessage)
      out ! error
    case _ => throw t
  }
}
