package services.plan.postgres

import models.plan.PlanNode
import upickle.Js
import PostgresParseKeys._

object PostgresNodeParser {
  def nodeFor(jsVal: Js.Value): PlanNode = jsVal match {
    case o: Js.Obj =>
      val params = o.value.toMap

      val children = params.get(keyPlans).map {
        case plans: Js.Arr => plans.value.map(nodeFor)
        case x => throw new IllegalStateException(s"Unable to parse plans from [$x]")
      }.getOrElse(Nil)

      val props = params.filterNot(_._1 == keyPlans).map(p => p._1 -> (p._2 match {
        case s: Js.Str => s.value
        case n: Js.Num => n.value.toString
        case s: Js.Arr => "[" + s.value.mkString(", ") + "]"
        case Js.False => "false"
        case Js.True => "true"
        case o: Js.Obj => "{" + o.value.map(v => v._1 + ": " + v._2).mkString(", ") + "}"
        case x => throw new IllegalStateException(s"Invalid param type [${x.getClass.getName}].")
      }))

      val costs = PlanNode.Costs(
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

      PlanNode(
        title = params.get(keyNodeType).fold("?") {
          case s: Js.Str => s.value
          case x => throw new IllegalStateException(x.toString)
        },
        nodeType = "?",
        costs = costs,
        properties = props,
        children = children
      )
    case x => throw new IllegalStateException(s"Invalid node type [${x.getClass.getName}]")
  }
}
