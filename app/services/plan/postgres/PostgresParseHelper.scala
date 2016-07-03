package services.plan.postgres

import models.plan.PlanNode
import services.plan.postgres.PostgresParseKeys._
import upickle.Js
import upickle.Js.Value

object PostgresParseHelper {
  def getOutput(o: Option[Value]) = o.map {
    case r: Js.Arr => r.value.map(_.str)
    case _ => Nil
  }

  def getCosts(params: Map[String, Value]) = PlanNode.Costs(
    estimatedRows = params.get(keyPlanRows) match {
    case Some(n: Js.Num) => n.value.toInt
    case _ => 0
  },
    actualRows = params.get(keyActualRows).map {
      case n: Js.Num => n.value.toInt
      case _ => 0
    },
    estimatedDuration = None,
    actualDuration = params.get(keyActualTotalTime).map {
      case n: Js.Num => n.value
      case _ => 0
    },
    estimatedCost = None,
    actualCost = params.get(keyTotalCost).map {
      case n: Js.Num => n.value.toInt
      case _ => 0
    }
  )
}
