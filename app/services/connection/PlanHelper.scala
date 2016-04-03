package services.connection

import java.util.UUID

import models.queries.DynamicQuery
import models.template.QueryPlanTemplate
import models.{ PlanErrorResponse, PlanResultResponse, ServerError }
import services.plan.PlanParseService
import utils.{ DateUtils, Logging }

trait PlanHelper extends Logging { this: ConnectionService =>
  def handleExplainQuery(queryId: UUID, sql: String) = {
    if (db.engine.explainSupported) {
      val explainSql = db.engine.explain(sql)
      log.info(s"Performing query action [explain] for sql [$explainSql].")

      val id = UUID.randomUUID
      val startMs = DateUtils.nowMillis
      sqlCatch(queryId, sql, startMs) { () =>
        implicit val engine = db.engine
        val result = db.query(DynamicQuery(explainSql))
        //log.info(s"Query result: [$result].")
        val durationMs = (DateUtils.nowMillis - startMs).toInt
        PlanParseService.parse(sql, queryId, PlanParseService.resultPlanString(result)) match {
          case Left(err) => out ! PlanErrorResponse(id, err, durationMs)
          case Right(planResponse) => out ! PlanResultResponse(id, planResponse, durationMs)
        }
      }
    } else {
      out ! ServerError("explain-not-supported", s"Explain is not avaialble for [${db.engine}].")
    }
  }

  def handleAnalyzeQuery(queryId: UUID, sql: String) = {
    out ! QueryPlanTemplate.testPlan("analyze", queryId)
  }
}
