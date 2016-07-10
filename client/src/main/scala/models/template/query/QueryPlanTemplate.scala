package models.template.query

import models.PlanResultResponse
import models.plan.PlanNode
import models.template.{Icons, StaticPanelTemplate}

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryPlanTemplate {
  def forPlan(pr: PlanResultResponse, dateIsoString: String) = {
    val content = div(id := pr.id.toString)(
      em("Executed ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateIsoString), s" in [${pr.durationMs}ms]"),
      div(cls := "plan-chart")(
        div(id := "", cls := "tree-container")(
          div(cls := "tree") {
            val costs = pr.result.node.costs
            val total = costs.cost.map(Left(_)).getOrElse {
              Right(costs.duration.orElse(costs.actualRows.map(_.toDouble)).getOrElse(costs.estimatedRows.toDouble))
            }
            ul(forNode(pr.result.node, "root-node", total))
          }
        ),
        div(cls := "clear")
      ),
      pre(cls := "plan-raw pre-wrap")(pr.result.raw)
    )

    StaticPanelTemplate.cardRow(
      content = content,
      iconAndTitle = Some(Icons.queryPlan -> "Query Plan"),
      actions = Seq(
        a(cls := "right plan-view-toggle theme-text", href := "#")("View Raw Plan"),
        div(style := "clear: both;")()
      ),
      showClose = false
    )
  }

  private[this] def forNode(node: PlanNode, className: String, total: Either[Int, Double], depth: Int = 0): Modifier = {
    val divContents = div(id := "plan-node-" + node.id, cls := "node z-depth-1")(
      div(cls := "node-percentage")(node.percentageString(total)),
      div(cls := "node-stat-divider")("|"),
      div(cls := "node-duration")(node.durationWithoutChildren.map { d =>
        BigDecimal.decimal(d).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble + "ms"
      }.orElse(node.costWithoutChildren.map(utils.NumberUtils.withCommas)).getOrElse(""): String),
      div(node.title),
      div(cls := "node-summary")(em(node.relation.getOrElse(""): String))
    )

    if (node.children.isEmpty) {
      li(cls := className)(divContents)
    } else {
      val kids = node.children.map(n => forNode(n, s"depth-$depth-child", total, depth + 1))
      li(cls := className)(divContents, ul(kids: _*))
    }
  }
}
