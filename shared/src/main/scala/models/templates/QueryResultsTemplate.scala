package models.templates

import models.QueryResult

import scalatags.Text.all._

object QueryResultsTemplate {
  def forResults(qr: QueryResult) = {
    val cardTitle = "Results"

    val tableHeader = thead(tr(qr.columns.map(c => th(c.name))))

    val tableBody = tbody(qr.data.map { r =>
      tr(r.map {
        case Some(v) => td(v)
        case None => td("")
        case null => td("wtfhardnull")
      })
    })

    val dataTable = table(tableHeader, tableBody)

    val card = div(cls := "card")(
      div(cls := "card-content")(
        span(cls := "card-title")(cardTitle),
        dataTable
      ),
      div(cls := "card-action")(
        a(href := "#")("Download")
      )
    )

    val wrapper = div(cls := "row") {
      div(cls := "col s12")(card)
    }

    wrapper
  }
}
