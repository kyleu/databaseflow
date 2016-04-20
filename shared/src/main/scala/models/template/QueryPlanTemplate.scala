package models.template

import models.PlanResultResponse
import models.plan.PlanNode

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryPlanTemplate {
  def forPlan(pr: PlanResultResponse, dateIsoString: String, dateFullString: String) = cardFor(pr, dateIsoString, dateFullString)

  private[this] def forNode(node: PlanNode, className: String): Modifier = if (node.children.isEmpty) {
    li(cls := className)(a(cls := "z-depth-1", href := "#")(node.title))
  } else {
    val kids = node.children.map(n => forNode(n, ""))
    li(cls := className)(a(cls := "z-depth-1", href := "#")(node.title), ul(kids: _*))
  }

  private[this] def cardFor(pr: PlanResultResponse, dateIsoString: String, dateFullString: String) = {
    val content = div(
      div(em("Executed ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${pr.durationMs}ms]")),
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

    div(id := pr.id.toString)(
      StaticPanelTemplate.cardRow(
        title = pr.result.name,
        content = content,
        icon = Some(Icons.queryPlan),
        actions = Some(Seq(
          a(href := "#")("Download"),
          a(cls := "right plan-view-toggle", href := "#")("View Raw Plan")
        ))
      )
    )
  }
}
