package models.template

import java.util.UUID

import models.PlanResultResponse
import models.plan.{ PlanNode, PlanResult }

import scalatags.Text.all._

object QueryPlanTemplate {
  def testPlan(action: String) = {
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
        name = "Test Query Plan",
        action = action,
        sql = "select * from something",
        asText = "...",
        node = node
      ),
      durationMs = 1000
    )
  }

  private[this] def forNode(node: PlanNode, className: String): Modifier = if (node.children.isEmpty) {
    li(cls := className)(a(href := "#")(node.title))
  } else {
    val kids = node.children.map(n => forNode(n, ""))
    li(cls := className)(a(href := "#")(node.title), ul(kids: _*))
  }

  private[this] def cardFor(pr: PlanResultResponse) = {
    div(id := pr.id.toString, cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(
          div(cls := "card-content")(
            span(cls := "card-title")(
              pr.result.name,
              i(cls := "right fa fa-close")
            ),
            div(
              div(id := "", cls := "tree-container")(
                div(cls := "tree")(
                  ul(forNode(pr.result.node, "root-node"))
                )
              )
            ),
            div(cls := "clear")
          ),
          div(cls := "card-action")(
            a(href := "#")("Download")
          )
        )
      )
    )
  }

  def forPlan(pr: PlanResultResponse) = {
    cardFor(pr)
  }
}
