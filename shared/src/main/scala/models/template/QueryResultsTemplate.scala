package models.template

import models.query.QueryResult

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryResultsTemplate {
  val actions = Seq(
    a(cls := "right results-sql-link theme-text", href := "#")("Show SQL"),
    a(cls := "results-download-link theme-text", href := "#")("Download")
  )

  def forResults(qr: QueryResult, dateIsoString: String, dateFullString: String, durationMs: Int) = div(
    em(s"${utils.NumberUtils.withCommas(qr.data.size)} rows returned ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${durationMs}ms]."),
    DataTableTemplate.forResults(qr),
    div(cls := "additional-results")(if (qr.moreRowsAvailable) {
      a(cls := "append-rows-link theme-text", href := "#")(s"Load ${qr.data.size} More Rows")
    } else {
      em("No more rows available")
    }),
    div(cls := "z-depth-1 query-result-sql")(
      pre(cls := "pre-wrap")(qr.sql)
    )
  )

  def forAppend(qr: QueryResult) = {
    val rows = DataTableTemplate.tableRows(qr)
    rows.map(_.render).mkString("\n")
  }

}
