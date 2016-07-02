package models.plan

import java.util.UUID

object PlanNode {
  case class Costs(
      estimatedRows: Int = 0, actualRows: Option[Int] = None,
      estimatedDuration: Option[Double] = None, actualDuration: Option[Double] = None,
      estimatedCost: Option[Int] = None, actualCost: Option[Int] = None
  ) {
    lazy val totalCost = actualCost.orElse(estimatedCost).getOrElse(0)
    lazy val estimatedRowsFactor = actualRows.map(estimatedRows / _)
    lazy val estimatedDurationFactor = actualDuration.flatMap(a => estimatedDuration.map(_ / a))
    lazy val estimatedCostFactor = actualCost.flatMap(c => estimatedCost.map(_ / c))
  }
}

case class PlanNode(
    id: UUID = UUID.randomUUID,
    title: String,
    nodeType: String,
    relation: Option[String] = None,
    output: Option[Seq[String]] = None,
    costs: PlanNode.Costs = PlanNode.Costs(),
    properties: Map[String, String] = Map.empty,
    children: Seq[PlanNode] = Nil
) {
  lazy val estimatedRowsWithoutChildren = costs.estimatedRows - children.map(_.costs.estimatedRows).sum
  lazy val actualRowsWithoutChildren = costs.actualRows.map(_ - children.flatMap(_.costs.actualRows).sum)

  lazy val estimatedDurationWithoutChildren = costs.estimatedDuration.map(_ - children.flatMap(_.costs.estimatedDuration).sum)
  lazy val actualDurationWithoutChildren = costs.actualDuration.map(_ - children.flatMap(_.costs.actualDuration).sum)
  lazy val durationWithoutChildren = actualDurationWithoutChildren.orElse(estimatedDurationWithoutChildren)

  lazy val estimatedCostWithoutChildren = costs.estimatedCost.map(_ - children.flatMap(_.costs.estimatedCost).sum)
  lazy val actualCostWithoutChildren = costs.actualCost.map(_ - children.flatMap(_.costs.actualCost).sum)
  lazy val costWithoutChildren = actualCostWithoutChildren.orElse(estimatedCostWithoutChildren)

  def withChildren(): Seq[PlanNode] = Seq(this) ++ children.flatMap(_.withChildren())

  def costPercentageString(totalCost: Int) = {
    val own = actualCostWithoutChildren.orElse(estimatedCostWithoutChildren).getOrElse(0)
    val pct = (own.toDouble / totalCost.toDouble) * 100
    val pctString = Math.round(pct)
    val est = if (costs.actualCost.isDefined) { "" } else { "~" }
    est + pctString + "%"
  }
}
