package models.template

import models.QueryResultResponse

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryResultsTemplate {
  val actions = Seq(
    a(cls := "right results-sql-link", href := "#")("Show SQL"),
    a(cls := "results-download-link", href := "#")("Download")
  )

  def forResults(qr: QueryResultResponse, dateIsoString: String, dateFullString: String) = div(
    em(s"${qr.result.data.size} rows returned ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${qr.durationMs}ms]."),
    DataTableTemplate.forResults(qr.result),
    div(cls := "z-depth-1 query-result-sql")(
      pre(cls := "pre-wrap")(qr.result.sql)
    )
  )
}
