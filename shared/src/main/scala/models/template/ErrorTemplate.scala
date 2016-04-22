package models.template

import models.{ PlanErrorResponse, QueryErrorResponse }

import scalatags.Text.all._
import scalatags.Text.tags2.time

object ErrorTemplate {
  def forQueryError(qe: QueryErrorResponse, dateIsoString: String, dateFullString: String) = {
    val status = p("Executed ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${qe.durationMs}ms].")
    div(id := qe.id.toString)(
      status,
      p(cls := "")(qe.error.message),
      if (qe.error.position.isEmpty) {
        ""
      } else {
        s"Error encountered at position [${qe.error.line.getOrElse(0)}:${qe.error.position.getOrElse(0)}]."
      }
    )
  }

  def forPlanError(pe: PlanErrorResponse, dateIsoString: String, dateFullString: String) = {
    val status = p("Executed ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${pe.durationMs}ms].")
    div(id := pe.id.toString)(
      status,
      p(cls := "")(pe.error.message)
    )
  }
}
