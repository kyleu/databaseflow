package services.connection

import java.util.UUID

import models.queries.DynamicQuery
import models.{ PlanErrorResponse, PlanResultResponse, ResponseMessage, ServerError }
import services.database.DatabaseWorkerPool
import services.plan.PlanParseService
import utils.{ DateUtils, ExceptionUtils, Logging }

trait PlanHelper extends Logging { this: ConnectionService =>
  def getResult(queryId: UUID, sql: String, explainSql: String, resultId: UUID) = {
    val startMs = DateUtils.nowMillis
    sqlCatch(queryId, sql, startMs, resultId) { () =>
      implicit val engine = db.engine
      val result = db.executeUnknown(DynamicQuery(explainSql))

      //log.info(s"Explain result: [$result].")
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      result match {
        case Left(rs) => PlanParseService.parse(sql, queryId, PlanParseService.resultPlanString(rs), startMs) match {
          case Left(err) => PlanErrorResponse(resultId, err, durationMs)
          case Right(planResponse) => PlanResultResponse(resultId, planResponse, durationMs)
        }
        case Right(x) => throw new IllegalStateException()
      }
    }
  }

  def handleExplainQuery(queryId: UUID, sql: String, resultId: UUID) = {
    db.engine.explain match {
      case Some(explain) =>
        def work() = {
          val explainSql = explain(sql)
          log.info(s"Performing query action [explain] for sql [$explainSql].")
          getResult(queryId, sql, explainSql, resultId)
        }
        def onSuccess(rm: ResponseMessage) = out ! rm
        def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "PlanExplainError", t)
        DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
      case None =>
        out ! ServerError("explain-not-supported", s"Explain is not avaialble for [${db.engine}].")
    }
  }

  def handleAnalyzeQuery(queryId: UUID, sql: String, resultId: UUID) = {
    db.engine.analyze match {
      case Some(analyze) =>
        def work() = {
          val analyzeSql = analyze(sql)
          log.info(s"Performing query action [analyze] for sql [$analyzeSql].")
          getResult(queryId, sql, analyzeSql, resultId)
        }
        def onSuccess(rm: ResponseMessage) = out ! rm
        def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "PlanAnalyzeError", t)
        DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
      case None =>
        out ! ServerError("analyze-not-supported", s"Analyze is not avaialble for [${db.engine}].")
    }
  }
}
