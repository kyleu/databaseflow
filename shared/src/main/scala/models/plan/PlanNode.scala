package models.plan

import java.util.UUID

object PlanNode {
  case class Costs(
      estimatedRows: Int = 0,
      actualRows: Option[Int] = None,
      duration: Option[Double] = None,
      cost: Option[Int] = None
  ) {
    lazy val estimatedRowsFactor = actualRows.map(estimatedRows / _)
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

  lazy val durationWithoutChildren = costs.duration.map(_ - children.flatMap(_.costs.duration).sum)
  lazy val costWithoutChildren = costs.cost.map(_ - children.flatMap(_.costs.cost).sum)

  def withChildren(): Seq[PlanNode] = Seq(this) ++ children.flatMap(_.withChildren())

  def percentageString(total: Either[Int, Double]) = {
    val own = costWithoutChildren.orElse(actualRowsWithoutChildren).getOrElse(estimatedRowsWithoutChildren)
    val totalCostReduced = total.fold(cost => cost.toDouble, rows => rows)
    val pct = (own.toDouble / totalCostReduced) * 100
    val pctString = Math.round(pct)
    val est = if (costs.cost.isDefined) { "" } else { "~" }
    est + pctString + "%"
  }
}
