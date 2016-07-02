package models.plan

object PlanNode {
  case class Costs(
      estimatedRows: Int = 0,
      actualRows: Option[Int] = None,

      estimatedDuration: Option[Double] = None,
      actualDuration: Option[Double] = None,

      estimatedCost: Option[Int] = None,
      actualCost: Option[Int] = None
  ) {
    lazy val estimatedRowsFactor = actualRows.map(estimatedRows / _)
    lazy val estimatedDurationFactor = actualDuration.flatMap(a => estimatedDuration.map(_ / a))
    lazy val estimatedCostFactor = actualCost.flatMap(c => estimatedCost.map(_ / c))
  }
}

case class PlanNode(
    title: String,
    nodeType: String,
    relation: Option[String],
    output: Option[Seq[String]],
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
}
