package services.plan.postgres

import models.plan.PlanNode
import services.plan.postgres.PostgresParseKeys._
import upickle.Js
import util.JsonUtils

object PostgresNodeParser {
  private[this] val blacklistedProperties = Seq(keyPlans, keyOutput)

  def nodeFor(json: Js.Value): PlanNode = {
    val params = json.obj

    val nodeType = params(keyNodeType).str
    val joinType = params.get(keyJoinType).map(_.str)
    val title = joinType.map(_ + " ").getOrElse("") + nodeType

    val children = params.get(keyPlans).map(_.arr.map(nodeFor)).getOrElse(Nil)

    val relation = params.get(keyRelationName).orElse(params.get(keyHashCondition)).map(_.str)
    val output = PostgresParseHelper.getOutput(params.get(keyOutput))
    val props = JsonUtils.toStringMap(params.filterNot(p => blacklistedProperties.contains(p._1)))
    val costs = PostgresParseHelper.getCosts(params)

    PlanNode(
      title = title,
      nodeType = nodeType,
      relation = relation,
      output = output,
      costs = costs,
      properties = props,
      children = children
    )
  }
}
