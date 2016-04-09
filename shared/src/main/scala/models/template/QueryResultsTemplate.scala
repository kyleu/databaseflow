package models.template

import models.QueryResultResponse

import scalatags.Text.all._
import scalatags.Text.tags2.time

object QueryResultsTemplate {
  def forResults(qr: QueryResultResponse, dateIsoString: String, dateFullString: String) = {
    val res = qr.result
    val cardTitle = res.title

    val card = div(cls := "card")(
      div(cls := "card-content")(
        span(cls := "card-title")(
          i(cls := s"title-icon fa ${Icons.queryResults}"),
          cardTitle,
          i(cls := s"right fa ${Icons.close}")
        ),
        p(s"${qr.result.data.size} rows returned ", time(cls := "timeago", "datetime".attr := dateIsoString)(dateFullString), s" in [${qr.durationMs}ms]."),
        DataTableTemplate.forResults(res),
        div(cls := "z-depth-1 query-result-sql")(
          pre(cls := "pre-wrap")(res.sql)
        )
      ),
      div(cls := "card-action")(
        a(cls := "right results-sql-link", href := "#")("Show SQL"),
        a(cls := "results-download-link", href := "#")("Download")
      )
    )

    val wrapper = div(id := qr.id.toString, cls := "row") {
      div(cls := "col s12")(card)
    }

    wrapper
  }
}
