package models.template

import models.PlanResultResponse
import models.plan.PlanNode

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryPlanTemplate {
  val actions = Seq(
    a(cls := "theme-text", href := "#")("Download"),
    a(cls := "right plan-view-toggle theme-text", href := "#")("View Raw Plan")
  )

  def forPlan(pr: PlanResultResponse, dateIsoString: String, dateFullString: String) = {
    val content = div(
      em("Executed ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${pr.durationMs}ms]"),
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
      iconAndTitle = Some(Icons.queryPlan -> pr.result.title),
      actions = QueryPlanTemplate.actions,
      showClose = false
    )
  }

  private[this] def forNode(node: PlanNode, className: String): Modifier = if (node.children.isEmpty) {
    li(cls := className)(div(cls := "node z-depth-1")(node.title))
  } else {
    val kids = node.children.map(n => forNode(n, ""))
    li(cls := className)(div(cls := "node z-depth-1")(node.title), ul(kids: _*))
  }
}
