package services.plan.postgres

import java.util.UUID

import models.plan.PlanNode
import services.plan.postgres.PostgresParseKeys._
import upickle.Js

object PostgresNodeParser {
  def nodeFor(jsVal: Js.Value): PlanNode = jsVal match {
    case o: Js.Obj =>
      val params = o.value.toMap

      val nodeType = params.get(keyNodeType) match {
        case Some(s: Js.Str) => s.value
        case x => throw new IllegalStateException("Missing node type parameter: " + x.toString)
      }

      val joinType = params.get(keyJoinType).map {
        case s: Js.Str => s.value
        case _ => ""
      }
      val title = joinType.map(_ + " ").getOrElse("") + nodeType

      val children = params.get(keyPlans).map {
        case plans: Js.Arr => plans.value.map(nodeFor)
        case x => throw new IllegalStateException(s"Unable to parse plans from [$x]")
      }.getOrElse(Nil)

      val relation = PostgresParseHelper.getRelation(params.get(keyRelationName).orElse(params.get(keyHashCondition)))
      val output = PostgresParseHelper.getOutput(params.get(keyOutput))
      val props = PostgresParseHelper.getProperties(params)
      val costs = PostgresParseHelper.getCosts(params)

      PlanNode(
        id = UUID.randomUUID,
        title = title,
        nodeType = nodeType,
        relation = relation,
        output = output,
        costs = costs,
        properties = props,
        children = children
      )
    case x => throw new IllegalStateException(s"Invalid node type [${x.getClass.getName}]")
  }
}
