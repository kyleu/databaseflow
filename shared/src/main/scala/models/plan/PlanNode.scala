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
    lazy val estimatesRowsFactor = actualRows.map(estimatedRows / _)
    lazy val estimatesDurationFactor = actualDuration.map(estimatedDuration / _)
    lazy val estimatesCostFactor = actualCost.map(estimatedCost / _)
  }
}

case class PlanNode(
  title: String,
  nodeType: String,
  costs: PlanNode.Costs = PlanNode.Costs(),
  properties: Map[String, String] = Map.empty,
  tags: Seq[String] = Nil,
  children: Seq[PlanNode] = Nil
)
