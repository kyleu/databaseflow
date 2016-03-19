package models.template

import java.util.UUID

import models.PlanResult
import models.plan.PlanNode

import scalatags.Text.all._

object QueryPlanTemplate {
  def testPlan(action: String) = {
    val node = PlanNode(title = "Parent", children = Seq(
      PlanNode(title = "Child A", children = Seq(
        PlanNode(title = "Grand Child A-1")
      )),
      PlanNode(title = "Child B", children = Seq(
        PlanNode(title = "Grand Child B-1"),
        PlanNode(title = "Grand Child B-2", children = Seq(
          PlanNode(title = "Great Grand Child B-2-1"),
          PlanNode(title = "Great Grand Child B-2-2"),
          PlanNode(title = "Great Grand Child B-2-3")
        )),
        PlanNode(title = "Grand Child B-3")
      ))
    ))

    PlanResult(
      id = UUID.randomUUID,
      name = "Test Query Plan",
      action = action,
      sql = "select * from something",
      asText = "...",
      node = node,
      created = 0L
    )
  }

  private[this] def forNode(node: PlanNode, className: String): Modifier = if (node.children.isEmpty) {
    li(cls := className)(a(href := "#")(node.title))
  } else {
    val kids = node.children.map(n => forNode(n, ""))
    li(cls := className)(a(href := "#")(node.title), ul(kids: _*))
  }

  private[this] def cardFor(pr: PlanResult) = {
    div(id := pr.id.toString, cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(
          div(cls := "card-content")(
            span(cls := "card-title")(
              pr.name,
              i(cls := "right fa fa-close")
            ),
            div(
              div(id := "", cls := "tree-container")(
                div(cls := "tree")(
                  ul(forNode(pr.node, "root-node"))
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

  def forPlan(pr: PlanResult) = {
    cardFor(pr)
  }
}
