package models.plan

case class PlanNode(
    slowest: Option[Boolean] = None,
    largest: Option[Boolean] = None,
    costliest: Option[Boolean] = None,

    estimatedRows: Int = 0,
    actualRows: Option[Int] = None,
    estimatedDuration: Int = 0,
    actualDuration: Option[Int] = None,
    estimatedCost: Int = 0,
    actualCost: Option[Int] = None,

    properties: Map[String, String],
    tags: Seq[String],

    children: Seq[PlanNode]
) {
  lazy val estimatesRowsFactor = actualRows.map(estimatedRows / _)
  lazy val estimatesDurationFactor = actualDuration.map(estimatedDuration / _)
  lazy val estimatesCostFactor = actualCost.map(estimatedCost / _)
}
