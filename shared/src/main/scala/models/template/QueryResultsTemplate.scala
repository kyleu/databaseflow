package models.template

import models.QueryResultResponse
import models.query.QueryResult

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryResultsTemplate {
  val actions = Seq(
    a(cls := "right results-sql-link", href := "#")("Show SQL"),
    a(cls := "results-download-link", href := "#")("Download")
  )

  def forResults(qr: QueryResult, dateIsoString: String, dateFullString: String, durationMs: Int) = div(
    em(s"${qr.data.size} rows returned ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${durationMs}ms]."),
    DataTableTemplate.forResults(qr),
    div(cls := "z-depth-1 query-result-sql")(
      pre(cls := "pre-wrap")(qr.sql)
    )
  )
}
