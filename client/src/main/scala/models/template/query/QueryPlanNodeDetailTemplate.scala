package models.template.query

import models.plan.PlanNode

import scalatags.Text.all._

object QueryPlanNodeDetailTemplate {
  def forNode(node: PlanNode, totalCost: Int): Modifier = {
    div(cls := "")(
      div(cls := "row")(
        div(cls := "col s12")(
          h5(div(cls := "right")(s"${node.costPercentageString(totalCost)} of cost"), node.title)
        )
      ),
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "z-depth-1")(
            table(cls := "bordered highlight") {
              tbody(
                tr(
                  th(colspan := "2")("Rows"),
                  th(colspan := "2")("Duration"),
                  th(colspan := "2")("Cost")
                ),
                tr(
                  th(style := "width: 16%")("Est"), th(style := "width: 17%")("Actual"),
                  th(style := "width: 17%")("Est"), th(style := "width: 17%")("Actual"),
                  th(style := "width: 16%")("Est"), th(style := "width: 17%")("Actual")
                ),
                tr(
                  td(utils.NumberUtils.withCommas(node.costs.estimatedRows)),
                  td(node.costs.actualRows.map(utils.NumberUtils.withCommas).getOrElse(""): String),

                  td(node.costs.estimatedDuration.map(_ + "ms").getOrElse(""): String),
                  td(node.costs.actualDuration.map(_ + "ms").getOrElse(""): String),

                  td(node.costs.estimatedCost.map(utils.NumberUtils.withCommas).getOrElse(""): String),
                  td(node.costs.actualCost.map(utils.NumberUtils.withCommas).getOrElse(""): String)
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
                  tr(th(colspan := 2)("Properties")),
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
                  tr(th(colspan := 2)("Output")),
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
