package models.template.view

import java.util.UUID

import models.engine.DatabaseEngine
import models.template.{Icons, StaticPanelTemplate}
import utils.Messages

import scalatags.Text.all._

object ViewDetailTemplate {
  private[this] def linksFor(engine: DatabaseEngine) = Seq(
    Some(a(cls := "view-data-link theme-text", href := "#")(Messages("query.view.first"))),
    if (engine.cap.explain.isDefined) { Some(a(cls := "explain-view-link theme-text", href := "#")(Messages("query.explain"))) } else { None },
    if (engine.cap.analyze.isDefined) { Some(a(cls := "analyze-view-link theme-text", href := "#")(Messages("query.analyze"))) } else { None }
  ).flatten

  def forView(engine: DatabaseEngine, queryId: UUID, tableName: String) = {
    val content = div(
      div(cls := "description")(""),
      ul(cls := "collapsible table-options", data("collapsible") := "expandable")(
        li(cls := "definition-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.definition}"), Messages("th.definition")),
          div(cls := "collapsible-body")(div(cls := "section-content")(Messages("general.loading")))
        ),
        li(cls := "columns-section initially-hidden")(
          div(cls := "collapsible-header")(i(cls := s"fa ${Icons.columns}"), Messages("th.columns"), span(cls := "badge")("")),
          div(cls := "collapsible-body")(div(cls := "section-content")(Messages("general.loading")))
        )
      )
    )

    div(id := s"panel-$queryId", cls := "workspace-panel")(
      StaticPanelTemplate.panelRow(content, iconAndTitle = Some(Icons.view -> span(tableName)), actions = linksFor(engine)),
      div(id := s"workspace-$queryId")
    )
  }
}
