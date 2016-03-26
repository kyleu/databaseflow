package models.template

import java.util.UUID

import models.schema.Table

import scalatags.Text.all._

object TableViewTemplate {
  def forTable(queryId: UUID, table: Table) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "card")(
            div(cls := "card-content")(
              span(cls := "card-title")(
                table.name,
                i(cls := "right fa fa-close")
              ),
              div("Here's some details about this table!")
            ),
            div(cls := "card-action")(
              a(cls := "view-data-link", href := "#")("View Data")
            )
          )
        )
      ),
      div(id := s"workspace-$queryId")
    )

  }
}
