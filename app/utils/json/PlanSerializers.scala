package utils.json

import models.plan.{PlanNode, QueryPlan}
import play.api.libs.json.Json

object PlanSerializers {
  implicit val planNodeFormat = Json.format[PlanNode]
  implicit val queryPlanFormat = Json.format[QueryPlan]
}
