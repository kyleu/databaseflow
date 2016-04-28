package models.template

import models.StatementResultResponse

import scalatags.Text.all._
import scalatags.Text.tags2.time

object StatementResultsTemplate {
  def forResults(srr: StatementResultResponse, dateIsoString: String, dateFullString: String) = {
    div(
      p(s"${srr.result.rowsAffected} rows affected ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${srr.durationMs}ms]."),
      div(cls := "z-depth-1 statement-result-sql")(
        pre(cls := "pre-wrap")(srr.result.sql)
      )
    )
  }
}
