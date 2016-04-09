package models.template

import java.util.UUID

import models.PlanResultResponse
import models.plan.{ PlanNode, PlanResult }

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryPlanTemplate {
  def testPlan(action: String, queryId: UUID) = {
    val node = PlanNode(title = "Parent", nodeType = "?", children = Seq(
      PlanNode(title = "Child A", nodeType = "?", children = Seq(
        PlanNode(title = "Grand Child A-1", nodeType = "?")
      )),
      PlanNode(title = "Child B", nodeType = "?", children = Seq(
        PlanNode(title = "Grand Child B-1", nodeType = "?"),
        PlanNode(title = "Grand Child B-2", nodeType = "?", children = Seq(
          PlanNode(title = "Great Grand Child B-2-1", nodeType = "?"),
          PlanNode(title = "Great Grand Child B-2-2", nodeType = "?"),
          PlanNode(title = "Great Grand Child B-2-3", nodeType = "?")
        )),
        PlanNode(title = "Grand Child B-3", nodeType = "?")
      ))
    ))

    PlanResultResponse(
      id = UUID.randomUUID,
      PlanResult(
        queryId = queryId,
        name = "Test Query Plan",
        action = action,
        sql = "select * from something",
        raw = "{ 'plan': 'test' }",
        node = node
      ),
      durationMs = 1000
    )
  }

  def forPlan(pr: PlanResultResponse, dateIsoString: String, dateFullString: String) = cardFor(pr, dateIsoString, dateFullString)

  private[this] def forNode(node: PlanNode, className: String): Modifier = if (node.children.isEmpty) {
    li(cls := className)(a(cls := "z-depth-1", href := "#")(node.title))
  } else {
    val kids = node.children.map(n => forNode(n, ""))
    li(cls := className)(a(cls := "z-depth-1", href := "#")(node.title), ul(kids: _*))
  }

  private[this] def cardFor(pr: PlanResultResponse, dateIsoString: String, dateFullString: String) = {
    div(id := pr.id.toString, cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(
          div(cls := "card-content")(
            span(cls := "card-title")(
              i(cls := s"title-icon fa ${Icons.queryPlan}"),
              pr.result.name,
              i(cls := s"right fa ${Icons.close}")
            ),
            div(em("Executed ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString))),
            div(cls := "plan-chart")(
              div(id := "", cls := "tree-container")(
                div(cls := "tree")(
                  ul(forNode(pr.result.node, "root-node"))
                )
              ),
              div(cls := "clear")
            ),
            pre(cls := "plan-raw pre-wrap")(pr.result.raw)
          ),
          div(cls := "card-action")(
            a(href := "#")("Download"),
            a(cls := "right plan-view-toggle", href := "#")("View Raw Plan")
          )
        )
      )
    )
  }
}
