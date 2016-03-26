package models.template

import java.util.UUID

import models.schema.Table

import scalatags.Text.all._

object TableViewTemplate {
  def forTable(queryId: UUID, table: Table) = {
    val description = table.description.getOrElse("")
    val links = Seq(
      Some(a(cls := "view-data-link", href := "#")("View Data")),
      table.foreignKeys.headOption.map(fk => a(cls := "foreign-keys-link", href := "#")("Foreign Keys")),
      table.indices.headOption.map(i => a(cls := "indices-link", href := "#")("Indices"))
    ).flatten

    div(id := s"panel-$queryId", cls := "workspace-panel")(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "card")(
            div(cls := "card-content")(
              span(cls := "card-title")(
                i(cls := "title-icon fa fa-folder-open-o"),
                table.name,
                i(cls := "right fa fa-close")
              ),
              div(description)
            ),
            div(cls := "card-action")(links: _*)
          )
        )
      ),
      div(id := s"workspace-$queryId")
    )

  }
}
