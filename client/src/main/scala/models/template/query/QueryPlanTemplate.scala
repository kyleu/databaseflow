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
    val summary = node.nodeType + node.relation.map(" on " + _).getOrElse("")
    val divContents = div(cls := "node z-depth-1")(
      div(cls := "node-title")(
        div(cls := "node-cost")(node.costWithoutChildren + "ms"),
        div(cls := "node-percentage")("%" + node.costPercentage),
        div(node.title),
        div(cls := "node-summary")(summary)
      ),
      div(cls := "node-details")(
        node.output match {
          case Some(o) => ul(cls := "")(o.map(x => li(x)))
          case None => span()
        },
        div(cls := "node-properties")(
          table(cls := "bordered highlight")(
            thead(tr("Name"), tr("Value")),
            tbody(node.properties.map(x => tr(td(x._1), td(x._2))).toSeq)
          )
        )
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
