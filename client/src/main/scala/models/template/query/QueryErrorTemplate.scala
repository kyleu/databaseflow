package models.template.query

import models.template.{Icons, StaticPanelTemplate}
import models.{PlanErrorResponse, QueryErrorResponse}
import utils.{Messages, TemplateUtils}

import scalatags.Text.all._

object QueryErrorTemplate {
  def forQueryError(qe: QueryErrorResponse, dateIsoString: String) = {
    val content = div(id := qe.id.toString)(
      a(href := "#", cls := "results-sql-link results-nav-link right theme-text")(Messages("th.sql")),
      p("Error encountered ", TemplateUtils.toTimeago(dateIsoString), s" in [${qe.error.elapsedMs}ms]."),
      div(cls := "z-depth-1 query-result-sql")(
        pre(cls := "pre-wrap")(qe.error.sql)
      ),
      p(cls := "error-detail-message")(qe.error.message),
      if (qe.error.index.isEmpty) {
        ""
      } else {
        Messages("query.index.error", qe.error.index.getOrElse(0))
      }
    )

    StaticPanelTemplate.card(
      content = content,
      showClose = false
    )
  }

  def forPlanError(pe: PlanErrorResponse, dateIsoString: String) = {
    val status = p("Executed ", TemplateUtils.toTimeago(dateIsoString), s" in [${pe.durationMs}ms].")
    val content = div(id := pe.id.toString)(
      status,
      p(cls := "")(pe.error.message)
    )

    StaticPanelTemplate.card(
      content = content,
      iconAndTitle = Some(Icons.error -> span(Messages("query.plan.error"))),
      showClose = false
    )
  }
}
