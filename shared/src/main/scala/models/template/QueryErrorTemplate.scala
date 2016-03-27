package models.template

import models.QueryErrorResponse

import scalatags.Text.all._

object QueryErrorTemplate {
  def forError(qe: QueryErrorResponse) = {
    val cardTitle = "Error"

    val card = div(cls := "card")(
      div(cls := "card-content")(
        span(cls := "card-title")(
          cardTitle,
          i(cls := s"right fa ${Icons.close}")
        ),
        p(s"Executed in [${qe.durationMs}ms]."),
        p(cls := "")(qe.error.message),
        if (qe.error.position.isEmpty) {
          ""
        } else {
          s"Error encountered at position [${qe.error.line.getOrElse(0)}:${qe.error.position.getOrElse(0)}]."
        }
      )
    )

    val wrapper = div(id := qe.id.toString, cls := "row") {
      div(cls := "col s12")(card)
    }

    wrapper
  }
}
