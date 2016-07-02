package services.plan.postgres

import models.plan.PlanNode
import services.plan.postgres.PostgresParseKeys._
import upickle.Js
import upickle.Js.Value

object PostgresParseHelper {
  def getRelation(o: Option[Value]) = o.map {
    case r: Js.Str => r.value
    case _ => ""
  }

  def getOutput(o: Option[Value]) = o.map {
    case r: Js.Arr => r.value.map {
      case s: Js.Str => s.value.trim
      case _ => throw new IllegalStateException("Output parameter is not an array.")
    }
    case _ => Nil
  }

  private[this] val blacklistedProperties = Seq(keyPlans, keyOutput)
  def getProperties(params: Map[String, Value]) = params.filterNot(p => blacklistedProperties.contains(p._1)).map(p => p._1 -> (p._2 match {
    case s: Js.Str => s.value
    case n: Js.Num => n.value.toString
    case s: Js.Arr => "[" + s.value.map {
      case js: Js.Str => js.value
      case _ => "?"
    }.mkString(", ") + "]"
    case Js.False => "false"
    case Js.True => "true"
    case o: Js.Obj => "{" + o.value.map(v => v._1 + ": " + v._2).mkString(", ") + "}"
    case x => throw new IllegalStateException(s"Invalid param type [${x.getClass.getName}].")
  }))

  def getCosts(params: Map[String, Value]) = PlanNode.Costs(
    estimatedRows = params.get(keyPlanRows) match {
    case Some(n: Js.Num) => n.value.toInt
    case _ => 0
  },
    actualRows = params.get(keyActualRows).map {
      case n: Js.Num => n.value.toInt
      case _ => 0
    },
    estimatedDuration = params.get("???") match {
      case Some(n: Js.Num) => n.value.toInt
      case _ => 0
    },
    actualDuration = params.get(keyActualTotalTime).map {
      case n: Js.Num => n.value.toInt
      case _ => 0
    },
    estimatedCost = params.get("???") match {
      case Some(n: Js.Num) => n.value.toInt
      case _ => 0
    },
    actualCost = params.get(keyTotalCost).map {
      case n: Js.Num => n.value.toInt
      case _ => 0
    }
  )
}
