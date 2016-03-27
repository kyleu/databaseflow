package models.template

import java.util.UUID

import scalatags.Text.all._

object TableDetailTemplate {
  def forTable(queryId: UUID, tableName: String) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "card")(
            div(cls := "card-content")(
              span(cls := "card-title")(
                i(cls := s"title-icon fa ${Icons.table}"),
                tableName,
                i(cls := s"right fa ${Icons.close}")
              )
            ),
            div(cls := "card-action")(
              a(cls := "view-data-link", href := "#")("View Data"),
              a(cls := "right indexes-link", href := "#")("Indexes"),
              a(cls := "right foreign-keys-link", href := "#")("Foreign Keys"),
              a(cls := "right columns-link", href := "#")("Columns")
            )
          )
        )
      ),
      div(id := s"workspace-$queryId")
    )
  }
}
