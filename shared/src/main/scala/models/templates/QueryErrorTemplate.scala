package models.templates

import models.QueryError

import scalatags.Text.all._

object QueryErrorTemplate {
  def forError(qe: QueryError) = {
    val cardTitle = "Error"

    val card = div(cls := "card")(
      div(cls := "card-content")(
        span(cls := "card-title")(
          cardTitle,
          i(cls := "right fa fa-close")
        ),
        p(s"Executed in [${qe.durationMs}ms]."),
        "Shit went wrong, yo."
      )
    )

    val wrapper = div(id := qe.id.toString, cls := "row") {
      div(cls := "col s12")(card)
    }

    wrapper
  }
}
