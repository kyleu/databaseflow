package models.templates

import java.util.UUID

import models.PlanResult
import models.plan.PlanNode

import scalatags.Text.all._

object QueryPlanTemplate {
  val testPlan = {
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
      sql = "select * from something",
      asText = "...",
      node = node,
      created = 0L
    )
  }

  private[this] def forNode(node: PlanNode): Modifier = if (node.children.isEmpty) {
    li(a(href := "#")(node.title))
  } else {
    val kids = node.children.map(forNode)
    li(a(href := "#")(node.title), ul(kids: _*))
  }

  private[this] def cardFor(pr: PlanResult) = {
    div(cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(
          div(cls := "card-content")(
            span(cls := "card-title")("Query Plan"),
            div(
              div(cls := "tree-container"),
              div(cls := "tree")(ul(
                forNode(pr.node)
              ))
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
