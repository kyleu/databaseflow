package services.plan.postgres

import io.circe.Json
import models.plan.PlanNode
import services.plan.postgres.PostgresParseKeys._
import util.JsonUtils

object PostgresNodeParser {
  private[this] val blacklistedProperties = Seq(keyPlans, keyOutput)

  def nodeFor(json: Json): PlanNode = {
    val params = json.asObject.get

    val nodeType = params(keyNodeType).get.asString.get
    val joinType = params(keyJoinType).map(_.asString.get)
    val title = joinType.map(_ + " ").getOrElse("") + nodeType

    val children = params(keyPlans).map(_.asArray.get.map(nodeFor)).getOrElse(Nil)

    val relation = params(keyRelationName).orElse(params(keyHashCondition)).map(_.asString.get)
    val output = PostgresParseHelper.getOutput(params(keyOutput))
    val props = JsonUtils.toStringMap(params.filter(p => !blacklistedProperties.contains(p._1)).toMap)
    val costs = PostgresParseHelper.getCosts(params.toMap)

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
