package models.plan

object PlanNode {
  case class Costs(
      estimatedRows: Int = 0,
      actualRows: Option[Int] = None,
      estimatedDuration: Int = 0,
      actualDuration: Option[Int] = None,
      estimatedCost: Int = 0,
      actualCost: Option[Int] = None
  ) {
    lazy val estimatedRowsFactor = actualRows.map(estimatedRows / _)
    lazy val estimatedDurationFactor = actualDuration.map(estimatedDuration / _)
    lazy val estimatedCostFactor = actualCost.map(estimatedCost / _)

    override def toString = {
      "" +
        (if (estimatedRows > 0) { s"EstRows: $estimatedRows " } else { "" }) +
        actualRows.map(a => s"ActRows: $a ").getOrElse("") +
        (if (estimatedDuration > 0) { s"EstDur: $estimatedDuration " } else { "" }) +
        actualDuration.map(a => s"ActDur: $a ").getOrElse("") +
        (if (estimatedCost > 0) { s"EstCost: $estimatedCost " } else { "" }) +
        actualCost.map(a => s"ActCost: $a ").getOrElse("")
    }
  }
}

case class PlanNode(
    title: String,
    nodeType: String,
    costs: PlanNode.Costs = PlanNode.Costs(),
    properties: Map[String, String] = Map.empty,
    children: Seq[PlanNode] = Nil
) {
  lazy val estimatedRowsWithoutChildren = costs.estimatedRows - children.map(_.costs.estimatedRows).sum
  lazy val actualRowsWithoutChildren = costs.actualRows.map(_ - children.flatMap(_.costs.actualRows).sum)
  lazy val estimatedDurationWithoutChildren = costs.estimatedDuration - children.map(_.costs.estimatedDuration).sum
  lazy val actualDurationWithoutChildren = costs.actualDuration.map(_ - children.flatMap(_.costs.actualDuration).sum)
  lazy val estimatedCostWithoutChildren = costs.estimatedCost - children.map(_.costs.estimatedCost).sum
  lazy val actualCostWithoutChildren = costs.actualCost.map(_ - children.flatMap(_.costs.actualCost).sum)
}
