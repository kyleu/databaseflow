package models.template

import models.PlanResultResponse
import models.plan.PlanNode

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryPlanTemplate {
  def forPlan(pr: PlanResultResponse, dateIsoString: String) = {
    val content = div(id := pr.id.toString)(
      em("Executed ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateIsoString), s" in [${pr.durationMs}ms]"),
      div(cls := "plan-chart")(
        div(id := "", cls := "tree-container")(
          div(cls := "tree")(
            ul(forNode(pr.result.node, "root-node"))
          )
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

  private[this] def forNode(node: PlanNode, className: String, depth: Int = 0): Modifier = {
    val divContents = div(cls := "node z-depth-1")(
      div(cls := "node-title")(node.title),
      div(cls := "node-details")(
        "Costs: ", node.costs.toString
      )
    )

    if (node.children.isEmpty) {
      li(cls := className)(divContents)
    } else {
      val kids = node.children.map(n => forNode(n, s"depth-$depth-child", depth + 1))
      li(cls := className)(divContents, ul(kids: _*))
    }
  }
}
