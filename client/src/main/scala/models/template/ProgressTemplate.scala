package models.template

import java.util.UUID

import utils.Messages

import scalatags.Text.all._

object ProgressTemplate {
  def loadingPanel(queryId: UUID, title: String, resultId: UUID) = {
    div(id := resultId.toString, cls := s"panel progress-$queryId")(
      StaticPanelTemplate.cardRow(
        content = div(
          div("Loading for ", span(cls := "timer")("0"), " seconds..."),
          div(cls := "cancel-query-link", a(href := "#")(Messages("general.cancel")))
        ),
        iconAndTitle = Some(Icons.loading + " " + Icons.spin -> span(title)),
        showClose = false
      )
    )
  }
}
