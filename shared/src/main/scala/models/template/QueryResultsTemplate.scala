package models.template

import models.QueryResultResponse

import scalatags.Text.all._

object QueryResultsTemplate {
  def forResults(qr: QueryResultResponse) = {
    val res = qr.result
    val cardTitle = res.title

    val tableHeader = thead(tr(res.columns.map(c => th(title := c.t)(c.name))))

    val tableBody = tbody(res.data.map { r =>
      tr(r.map {
        case Some(v) if v.isEmpty => td(em("empty string"))
        case Some(v) => td(v)
        case None => td(title := "Null")("∅")
        case null => td("null-bug")
      })
    })

    val data = if (res.columns.isEmpty || res.data.isEmpty) {
      em("No rows returned.")
    } else {
      div(cls := "fixed-action-btn horizontal", style := "bottom: 45px; right: 24px;")(
        a(cls := "btn-floating btn-large red")(
          i(cls := "large fa fa-stuff")
        ),
        ul(
          li(a(cls := "btn-floating red")(i(cls := "fa fa-stuff"))),
          li(a(cls := "btn-floating yellow darken-1")(i(cls := "fa fa-stuff"))),
          li(a(cls := "btn-floating green")(i(cls := "fa fa-stuff"))),
          li(a(cls := "btn-floating blue")(i(cls := "fa fa-stuff")))
        )
      )
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
