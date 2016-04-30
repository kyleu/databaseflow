package models.template

import models.{ PlanErrorResponse, QueryErrorResponse }

import scalatags.Text.all._
import scalatags.Text.tags2.time

object ErrorTemplate {
  def forQueryError(qe: QueryErrorResponse, dateIsoString: String, dateFullString: String) = {
    val content = div(id := qe.id.toString)(
      p("Executed ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${qe.durationMs}ms]."),
      p(cls := "")(qe.error.message),
      if (qe.error.position.isEmpty) {
        ""
      } else {
        s"Error encountered at position [${qe.error.line.getOrElse(0)}:${qe.error.position.getOrElse(0)}]."
      }
    )

    StaticPanelTemplate.cardRow(
      content = content,
      iconAndTitle = Some(Icons.error -> qe.error.title),
      showClose = false
    )
  }

  def forPlanError(pe: PlanErrorResponse, dateIsoString: String, dateFullString: String) = {
    val status = p("Executed ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${pe.durationMs}ms].")
    val content = div(id := pe.id.toString)(
      status,
      p(cls := "")(pe.error.message)
    )

    StaticPanelTemplate.cardRow(
      content = content,
      iconAndTitle = Some(Icons.error -> pe.error.title),
      showClose = false
    )
  }
}
