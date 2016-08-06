package models.template.query

import models.plan.PlanNode
import utils.{Messages, NumberUtils}

import scalatags.Text.all._

object QueryPlanNodeDetailTemplate {
  def forNode(node: PlanNode, total: Either[Int, Double]): Modifier = {
    div(cls := "")(
      div(cls := "row")(
        div(cls := "col s12")(
          h5(div(cls := "right")(s"${node.percentageString(total)} of total"), node.title)
        )
      ),
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "z-depth-1")(
            table(cls := "bordered highlight") {
              tbody(
                tr(
                  th(style := "width: 25%")(Messages("query.plan.estimated.rows")), th(style := "width: 25%")("Actual Rows"),
                  th(style := "width: 25%")("Duration"), th(style := "width: 25%")(Messages("th.cost"))
                ),
                tr(
                  td(NumberUtils.withCommas(node.costs.estimatedRows)),
                  td(node.costs.actualRows.map(NumberUtils.withCommas).getOrElse(""): String),

                  td(node.costs.duration.map(_ + "ms").getOrElse(""): String),
                  td(node.costs.cost.map(NumberUtils.withCommas).getOrElse(""): String)
                )
              )
            }
          )
        )
      ),
      if (node.properties.isEmpty) {
        div()
      } else {
        div(cls := "row")(
          div(cls := "col s12")(
            div(cls := "z-depth-1")(
              table(cls := "bordered highlight")(
                tbody(
                  tr(th(colspan := 2)(Messages("th.properties"))),
                  node.properties.map { p =>
                    tr(td(style := "white-space: nowrap;")(p._1), td(p._2))
                  }.toSeq
                )
              )
            )
          )
        )
      },
      div(cls := "row")(
        node.output match {
          case None => div()
          case Some(o) => div(cls := "col s12")(
            div(cls := "z-depth-1")(
              table(cls := "bordered highlight") {
                tbody(
                  tr(th(colspan := 2)(Messages("th.output"))),
                  o.map(x => tr(td(x)))
                )
              }
            )
          )
        }
      )
    )
  }
}
