package services.plan.oracle

import java.util.UUID

import models.plan.PlanNode
import org.joda.time.format.PeriodFormatterBuilder

object OracleParseHelper {
  case class PlanEntry(
      id: Int,
      operation: String,
      depth: Int,
      name: Option[String],
      rows: String,
      bytes: String,
      cost: Int,
      cpu: Int,
      time: String,
      children: Seq[PlanEntry] = Nil
  )

  def nodeFor(s: Seq[PlanEntry]): PlanNode = {
    val me = s.headOption.getOrElse(throw new IllegalArgumentException("Node encountered empty list."))

    val children = s.tail.foldLeft(Seq.empty[Seq[PlanEntry]]) { (x, y) =>
      if (y.depth == me.depth + 1) {
        x :+ Seq(y)
      } else if (y.depth == me.depth) {
        throw new IllegalStateException("Invalid depth.")
      } else {
        if (x.isEmpty) {
          Seq(Seq(y))
        } else {
          x.dropRight(1) :+ (x.last :+ y)
        }
      }
    }

    PlanNode(
      title = me.name.getOrElse(me.operation),
      nodeType = me.operation,
      children = children.map(nodeFor),
      costs = PlanNode.Costs(
        estimatedRows = stringToInt(me.rows),
        duration = Some(durationToDouble(me.time)),
        cost = Some(me.cost)
      )
    )
  }

  def stringToInt(s: String) = s match {
    case _ if s.endsWith("K") => s.dropRight(1).toInt * 1000
    case _ if s.endsWith("M") => s.dropRight(1).toInt * 1000000
    case _ => s.toInt
  }

  private[this] val formatter = new PeriodFormatterBuilder().appendHours().appendLiteral(":").appendMinutes().appendLiteral(":").appendSeconds().toFormatter

  private[this] def durationToDouble(s: String) = formatter.parsePeriod(s).toStandardSeconds.getSeconds.toDouble
}
