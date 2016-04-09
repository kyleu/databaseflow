package models.template

import models.{ PlanErrorResponse, QueryErrorResponse }

import scalatags.Text.all._
import scalatags.Text.tags2.time

object ErrorTemplate {
  def forQueryError(qe: QueryErrorResponse, dateIsoString: String, dateFullString: String) = {
    val cardTitle = "Query Error"

    val card = div(cls := "card")(
      div(cls := "card-content")(
        span(cls := "card-title")(
          cardTitle,
          i(cls := s"right fa ${Icons.close}")
        ),
        p("Executed ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${qe.durationMs}ms]."),
        p(cls := "")(qe.error.message),
        if (qe.error.position.isEmpty) {
          ""
        } else {
          s"Error encountered at position [${qe.error.line.getOrElse(0)}:${qe.error.position.getOrElse(0)}]."
        }
      )
    )

    val wrapper = div(id := qe.id.toString, cls := "row") {
      div(cls := "col s12")(card)
    }

    wrapper
  }

  def forPlanError(pe: PlanErrorResponse, dateIsoString: String, dateFullString: String) = {
    val cardTitle = "Plan Error"

    val card = div(cls := "card")(
      div(cls := "card-content")(
        span(cls := "card-title")(
          cardTitle,
          i(cls := s"right fa ${Icons.close}")
        ),
        p("Executed ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${pe.durationMs}ms]."),
        p(cls := "")(pe.error.message)
      )
    )

    val wrapper = div(id := pe.id.toString, cls := "row") {
      div(cls := "col s12")(card)
    }

    wrapper
  }
}
