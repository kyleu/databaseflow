package models.template

import models.{ PlanErrorResponse, QueryErrorResponse }

import scalatags.Text.all._
import scalatags.Text.tags2.time

object ErrorTemplate {
  def forQueryError(qe: QueryErrorResponse, dateIsoString: String, dateFullString: String) = {
    val cardTitle = "Query Error"

    val content = div(
      p("Executed ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${qe.durationMs}ms]."),
      p(cls := "")(qe.error.message),
      if (qe.error.position.isEmpty) {
        ""
      } else {
        s"Error encountered at position [${qe.error.line.getOrElse(0)}:${qe.error.position.getOrElse(0)}]."
      }
    )

    StaticPanelTemplate.cardRow(cardTitle, content, Some(Icons.error))
  }

  def forPlanError(pe: PlanErrorResponse, dateIsoString: String, dateFullString: String) = {
    val cardTitle = "Plan Error"

    val content = div(
      span(cls := "card-title")(
        cardTitle,
        i(cls := s"right fa ${Icons.close}")
      ),
      p("Executed ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${pe.durationMs}ms]."),
      p(cls := "")(pe.error.message)
    )

    StaticPanelTemplate.cardRow(cardTitle, content, Some(Icons.error))
  }
}
