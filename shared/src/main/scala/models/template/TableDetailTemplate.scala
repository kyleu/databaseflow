package models.template

import java.util.UUID

import scalatags.Text.all._

object TableDetailTemplate {
  def forTable(queryId: UUID, tableName: String) = {
    val content = div(
      div(cls := "description")(""),
      ul(cls := "collapsible table-options", data("collapsible") := "expandable")(
        li(cls := "definition-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.definition}"), "Definition"),
          div(cls := "collapsible-body")(div(cls := "section-content")("Loading..."))
        ),
        li(cls := "columns-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.columns}"), "Columns", span(cls := "badge")("")),
          div(cls := "collapsible-body")(div(cls := "section-content")("Loading..."))
        ),
        li(cls := "indexes-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.indexes}"), "Indexes", span(cls := "badge")("")),
          div(cls := "collapsible-body")(div(cls := "section-content")("Loading..."))
        ),
        li(cls := "foreign-keys-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.foreignKeys}"), "Foreign Keys", span(cls := "badge")("")),
          div(cls := "collapsible-body")(div(cls := "section-content")("Loading..."))
        )
      )
    )
    val actions = Seq(
      a(cls := "view-data-link", href := "#")("View First 100 Rows")
    )
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      StaticPanelTemplate.cardRow(tableName, content, icon = Some(Icons.table), actions = Some(actions)),
      div(id := s"workspace-$queryId")
    )
  }
}
