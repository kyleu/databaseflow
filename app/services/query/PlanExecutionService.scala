package services.query

import java.util.UUID

import akka.actor.ActorRef
import models.engine.rdbms.Oracle
import models.queries.DynamicQuery
import models.{ PlanErrorResponse, PlanResultResponse, ResponseMessage, ServerError }
import services.database.{ DatabaseConnection, DatabaseWorkerPool }
import services.plan.PlanParseService
import utils.{ DateUtils, ExceptionUtils, JdbcUtils, Logging }

object PlanExecutionService extends Logging {
  private[this] def getResult(db: DatabaseConnection, queryId: UUID, sql: String, explainSql: String, resultId: UUID) = {
    val startMs = DateUtils.nowMillis
    JdbcUtils.sqlCatch(queryId, sql, startMs, resultId) { () =>
      db.transaction { tx =>
        implicit val engine = db.engine

        val initialResult = tx.executeUnknown(DynamicQuery(explainSql)) match {
          case Left(res) => res
          case Right(i) => DynamicQuery.Results(Nil, Nil)
        }

        val result = db.engine match {
          case Oracle => tx.query(DynamicQuery("select plan_table_output from table(dbms_xplan.display())"))
          case _ => initialResult
        }

        val durationMs = (DateUtils.nowMillis - startMs).toInt
        PlanParseService.parse(sql, queryId, PlanParseService.resultPlanString(result), startMs) match {
          case Right(planResponse) => PlanResultResponse(resultId, planResponse, durationMs)
          case Left(err) => PlanErrorResponse(resultId, err, durationMs)
        }
      }
    }
  }

  def handleExplainQuery(db: DatabaseConnection, queryId: UUID, sql: String, resultId: UUID, out: ActorRef): Unit = {
    db.engine.explain match {
      case Some(explain) =>
        def work() = {
          val explainSql = explain(sql)
          log.info(s"Performing query action [explain] with resultId [$resultId] for query [$queryId] with sql [$explainSql].")
          getResult(db, queryId, sql, explainSql, resultId)
        }
        def onSuccess(rm: ResponseMessage) = out ! rm
        def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "PlanExplainError", t)
        DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
      case None =>
        out ! ServerError("explain-not-supported", s"Explain is not avaialble for [${db.engine}].")
    }
  }

  def handleAnalyzeQuery(db: DatabaseConnection, queryId: UUID, sql: String, resultId: UUID, out: ActorRef): Unit = {
    db.engine.analyze match {
      case Some(analyze) =>
        def work() = {
          val analyzeSql = analyze(sql)
          log.info(s"Performing query action [analyze] with resultId [$resultId] for query [$queryId] with sql [$analyzeSql].")
          getResult(db, queryId, sql, analyzeSql, resultId)
        }
        def onSuccess(rm: ResponseMessage) = out ! rm
        def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "PlanAnalyzeError", t)
        DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
      case None =>
        out ! ServerError("analyze-not-supported", s"Analyze is not avaialble for [${db.engine}].")
    }
  }
}
