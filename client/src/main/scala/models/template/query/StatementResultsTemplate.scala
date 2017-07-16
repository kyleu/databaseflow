package models.template.query

import java.util.UUID

import models.query.QueryResult
import models.template.StaticPanelTemplate
import util.{Messages, TemplateUtils}

import scalatags.Text.all._

object StatementResultsTemplate {
  def forStatementResults(qr: QueryResult, dateIsoString: String, durationMs: Int, resultId: UUID) = {
    val rowLabel = if (qr.rowsAffected == 1) { "row" } else { "rows" }
    val content = div(id := s"$resultId")(
      a(href := "#", cls := "results-sql-link right theme-text")(Messages("th.sql")),
      p(s"${qr.rowsAffected} $rowLabel affected ", TemplateUtils.toTimeago(dateIsoString), s" in [${durationMs}ms]."),
      div(cls := "z-depth-1 statement-result-sql")(
        pre(cls := "pre-wrap")(qr.sql)
      )
    )

    StaticPanelTemplate.card(
      content = content,
      showClose = false
    )
  }
}
