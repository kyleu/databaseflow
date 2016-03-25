package models.template

import models.QueryResultResponse

import scalatags.Text.all._

object QueryResultsTemplate {
  def forResults(qr: QueryResultResponse) = {
    val res = qr.result
    val cardTitle = res.title

    val card = div(cls := "card")(
      div(cls := "card-content")(
        span(cls := "card-title")(
          cardTitle,
          i(cls := "right fa fa-close")
        ),
        p(s"Executed in [${qr.durationMs}ms]."),
        DataTableTemplate.forResults(res.columns, res.data)
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
