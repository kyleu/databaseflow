package models.template

import models.QueryResultResponse

import scalatags.Text.all._

object QueryResultsTemplate {
  def forResults(qr: QueryResultResponse) = {
    val cardTitle = "Results"

    val tableHeader = thead(tr(qr.result.columns.map(c => th(title := c.t)(c.name))))

    val tableBody = tbody(qr.result.data.map { r =>
      tr(r.map {
        case Some(v) if v.isEmpty => td(em("empty string"))
        case Some(v) => td(v)
        case None => td("∅")
        case null => td("null-bug")
      })
    })

    val data = if (qr.result.columns.isEmpty || qr.result.data.isEmpty) {
      em("No rows returned.")
    } else {
      div(cls := "query-result-table")(table(cls := "bordered highlight responsive-table")(tableHeader, tableBody))
    }

    val card = div(cls := "card")(
      div(cls := "card-content")(
        span(cls := "card-title")(
          cardTitle,
          i(cls := "right fa fa-close")
        ),
        p(s"Executed in [${qr.durationMs}ms]."),
        data
      ),
      div(cls := "card-action")(
        a(href := "#")("Download")
      )
    )

    val wrapper = div(id := qr.id.toString, cls := "row") {
      div(cls := "col s12")(card)
    }

    wrapper
  }
}
