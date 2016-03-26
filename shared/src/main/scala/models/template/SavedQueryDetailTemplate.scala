package models.template

import java.util.UUID

import models.query.SavedQuery

import scalatags.Text.all._

object SavedQueryDetailTemplate {
  def forSavedQuery(queryId: UUID, savedQuery: SavedQuery) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "card")(
            div(cls := "card-content")(
              span(cls := "card-title")(
                i(cls := "title-icon fa fa-folder-open-o"),
                savedQuery.title,
                i(cls := "right fa fa-close")
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
