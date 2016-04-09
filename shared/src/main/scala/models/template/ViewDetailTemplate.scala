package models.template

import java.util.UUID

import scalatags.Text.all._

object ViewDetailTemplate {
  def forView(queryId: UUID, tableName: String) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "card")(
            div(cls := "card-content")(
              span(cls := "card-title")(
                i(cls := s"title-icon fa ${Icons.view}"),
                tableName,
                i(cls := s"right fa ${Icons.close}")
              ),
              div(cls := "description")(""),
              div(cls := "summary")("")
            ),
            div(cls := "card-action")(
              a(cls := "view-data-link", href := "#")("View Data"),
              a(cls := "right definition-link initially-hidden", href := "#")("Definition"),
              a(cls := "right columns-link initially-hidden", href := "#")("Columns")
            )
          )
        )
      ),
      div(id := s"workspace-$queryId")
    )
  }
}
