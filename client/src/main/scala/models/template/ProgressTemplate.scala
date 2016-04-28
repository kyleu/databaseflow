package models.template

import java.util.UUID

import scalatags.Text.all._

object ProgressTemplate {
  def loadingPanel(queryId: UUID, title: String, resultId: UUID) = {
    div(id := resultId.toString, cls := s"panel progress-$queryId")(
      StaticPanelTemplate.cardRow(
        title = title,
        content = div("Loading for ", span(cls := "timer")("0"), " seconds..."),
        icon = Some(Icons.loading + " " + Icons.spin),
        showClose = false
      )
    )
  }
}
