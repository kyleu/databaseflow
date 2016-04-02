package models.template

import java.util.UUID

import scalatags.Text.all._

object ModelListTemplate {
  def forModels(queryId: UUID, key: String, name: String) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "card")(
            div(cls := "card-content")(
              span(cls := "card-title")(
                i(cls := s"title-icon fa ${Icons.list}"),
                name,
                i(cls := s"right fa ${Icons.close}")
              )
            ),
            div(cls := "card-action")()
          )
        )
      ),
      div(id := s"workspace-$queryId")
    )
  }
}
