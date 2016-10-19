package models.template.tbl

import java.util.UUID

import models.template.{Icons, StaticPanelTemplate}
import utils.Messages

import scalatags.Text.all._

object TableDetailTemplate {
  def forTable(queryId: UUID, tableName: String) = {
    val content = div(
      div(cls := "description initially-hidden")(""),
      div(cls := "row-count initially-hidden")(em(span("Unknown"), " estimated rows")),
      ul(cls := "collapsible table-options", data("collapsible") := "expandable")(
        li(cls := "definition-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.definition}"), Messages("th.definition")),
          div(cls := "collapsible-body")(div(cls := "section-content")(Messages("general.loading")))
        ),
        li(cls := "columns-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.columns}"), Messages("th.columns"), span(cls := "badge")("")),
          div(cls := "collapsible-body")(div(cls := "section-content")(Messages("general.loading")))
        ),
        li(cls := "indexes-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.indexes}"), Messages("th.indexes"), span(cls := "badge")("")),
          div(cls := "collapsible-body")(div(cls := "section-content")(Messages("general.loading")))
        ),
        li(cls := "foreign-keys-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.foreignKeys}"), Messages("th.foreign.keys"), span(cls := "badge")("")),
          div(cls := "collapsible-body")(div(cls := "section-content")(Messages("general.loading")))
        )
      )
    )
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      StaticPanelTemplate.row(StaticPanelTemplate.panel(content, iconAndTitle = Some(Icons.table -> span(tableName)), actions = Seq(
        a(cls := "insert-data-link theme-text right", href := "#")(Messages("query.insert")),
        a(cls := "query-open-link theme-text right", href := "#")(Messages("query.open.query")),
        a(cls := "view-data-link theme-text", href := "#")(Messages("query.view.first"))
      ))),
      div(id := s"workspace-$queryId")
    )
  }
}
