package models.template

import java.util.UUID

import scalatags.Text.all._

object ViewDetailTemplate {
  def forView(queryId: UUID, viewName: String) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "card")(
            div(cls := "card-content")(
              span(cls := "card-title")(
                i(cls := "title-icon fa fa-bar-chart"),
                viewName,
                i(cls := "right fa fa-close")
              )
            ),
            div(cls := "card-action")(a(cls := "view-data-link", href := "#")("View Data"))
          )
        )
      ),
      div(id := s"workspace-$queryId")
    )
  }
}
