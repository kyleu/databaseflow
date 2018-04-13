package services.plan.postgres

import io.circe.Json
import models.plan.PlanNode
import services.plan.postgres.PostgresParseKeys._

object PostgresParseHelper {
  def getOutput(o: Option[Json]) = o.map { j =>
    j.asArray.get.map(_.asString.get)
  }

  def getCosts(params: Map[String, Json]) = PlanNode.Costs(
    estimatedRows = params.get(keyPlanRows) match {
      case Some(n) if n.isNumber => n.asNumber.get.toInt.get
      case _ => 0
    },
    actualRows = params.get(keyActualRows).map {
      case n if n.isNumber => n.asNumber.get.toInt.get
      case _ => 0
    },
    duration = params.get(keyActualTotalTime).map {
      case n if n.isNumber => n.asNumber.get.toDouble
      case _ => 0
    },
    cost = params.get(keyTotalCost).map {
      case n if n.isNumber => n.asNumber.get.toInt.get
      case _ => 0
    }
  )
}
