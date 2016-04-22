package models.template

import java.util.UUID

import models.QueryResultResponse

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryResultsTemplate {
  def loadingPanel(queryId: UUID, title: String, resultId: UUID) = div(id := resultId.toString)(
    StaticPanelTemplate.cardRow(
      title = title,
      content = div(id := "content")("Loading..."),
      icon = Some(Icons.queryResults),
      actions = Some(Seq(
        a(cls := "right results-sql-link", href := "#")("Show SQL"),
        a(cls := "results-download-link", href := "#")("Download")
      ))
    )
  )

  def status(qr: QueryResultResponse, dateIsoString: String, dateFullString: String) = {
    p(s"${qr.result.data.size} rows returned ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${qr.durationMs}ms].")
  }

  def forResults(qr: QueryResultResponse) = div(
    DataTableTemplate.forResults(qr.result),
    div(cls := "z-depth-1 query-result-sql")(
      pre(cls := "pre-wrap")(qr.result.sql)
    )
  )
}
