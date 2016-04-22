package models.template

import models.StatementResultResponse

import scalatags.Text.all._
import scalatags.Text.tags2.time

object StatementResultsTemplate {
  def status(srr: StatementResultResponse, dateIsoString: String, dateFullString: String) = {
    p(s"${srr.result.rowsAffected} rows affected ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${srr.durationMs}ms].")
  }

  def forResults(srr: StatementResultResponse) = {
    val res = srr.result
    val cardTitle = res.title

    val content = div(
      div(cls := "z-depth-1 statement-result-sql")(
        pre(cls := "pre-wrap")(res.sql)
      )
    )

    div(id := srr.id.toString)(
      StaticPanelTemplate.cardRow(
        title = cardTitle,
        content = content,
        icon = Some(Icons.statementResults)
      )
    )
  }
}
