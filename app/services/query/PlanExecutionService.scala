package services.query

import java.util.UUID

import akka.actor.ActorRef
import models.database.Queryable
import models.engine.DatabaseEngine
import models.engine.DatabaseEngine.Oracle
import models.queries.dynamic.DynamicQuery
import models.{PlanErrorResponse, PlanResultResponse, ResponseMessage, ServerError}
import services.database.DatabaseWorkerPool
import services.plan.PlanParseService
import utils.{DateUtils, ExceptionUtils, JdbcUtils, Logging}

object PlanExecutionService extends Logging {
  private[this] def getResult(db: Queryable, engine: DatabaseEngine, queryId: UUID, sql: String, explainSql: String, resultId: UUID) = {
    val startMs = DateUtils.nowMillis
    JdbcUtils.sqlCatch(queryId, sql, startMs, resultId, 0) { () =>
      db.transaction { tx =>
        implicit val e = engine

        val initialResult = tx.executeUnknown(DynamicQuery(explainSql)) match {
          case Left(res) => res
          case Right(i) => DynamicQuery.Results(Nil, Nil)
        }

        val result = engine match {
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

  def handleExplainQuery(db: Queryable, engine: DatabaseEngine, queryId: UUID, sql: String, params: Map[String, String], resultId: UUID, out: ActorRef) = {
    engine.cap.explain match {
      case Some(explain) =>
        def work() = {
          val merged = ParameterService.merge(sql, params)
          val explainSql = explain(merged)
          log.info(s"Performing query action [explain] with resultId [$resultId] for query [$queryId] with sql [$explainSql].")
          getResult(db, engine, queryId, sql, explainSql, resultId)
        }
        def onSuccess(rm: ResponseMessage) = out ! rm
        def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "PlanExplainError", t)
        DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
      case None =>
        out ! ServerError("Not Supported", s"Explain is not avaialble for [$engine].")
    }
  }

  def handleAnalyzeQuery(db: Queryable, engine: DatabaseEngine, queryId: UUID, sql: String, params: Map[String, String], resultId: UUID, out: ActorRef) = {
    engine.cap.analyze match {
      case Some(analyze) =>
        def work() = {
          val merged = ParameterService.merge(sql, params)
          val analyzeSql = analyze(merged)
          log.info(s"Performing query action [analyze] with resultId [$resultId] for query [$queryId] with sql [$analyzeSql].")
          getResult(db, engine, queryId, sql, analyzeSql, resultId)
        }
        def onSuccess(rm: ResponseMessage) = out ! rm
        def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "PlanAnalyzeError", t)
        DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
      case None =>
        out ! ServerError("Not Supported", s"Analyze is not avaialble for [$engine].")
    }
  }
}
