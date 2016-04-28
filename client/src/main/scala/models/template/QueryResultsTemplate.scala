package models.template

import models.query.QueryResult

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryResultsTemplate {
  def forResults(qr: QueryResult, dateIsoString: String, dateFullString: String, durationMs: Int) = div(
    div(cls := "query-result-details z-depth-1")(
      h4("Activity"),
      div(cls := "activity-container")(
        em(
          s"${utils.NumberUtils.withCommas(qr.data.size)} rows returned ",
          time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString),
          s" in [${durationMs}ms]."
        )
      ),
      h4("SQL"),
      pre(cls := "pre-wrap")(qr.sql)
    ),

    DataTableTemplate.forResults(qr),

    div(cls := "additional-results")(
      a(cls := "append-rows-link theme-text initially-hidden", data("offset") := "0", href := "#")(s"Load ${qr.data.size} More Rows"),
      em(cls := "no-rows-remaining initially-hidden")("No more rows available")
    )
  )

  def forAppend(qr: QueryResult) = {
    val rows = DataTableTemplate.tableRows(qr)
    rows.map(_.render).mkString("\n")
  }

}
