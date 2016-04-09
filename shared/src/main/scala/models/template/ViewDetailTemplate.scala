package models.template

import java.util.UUID

import models.engine.DatabaseEngine

import scalatags.Text.all._

object ViewDetailTemplate {
  private[this] def linksFor(engine: DatabaseEngine) = Seq(
    Some(a(cls := "view-data-link", href := "#")("View Data")),
    if (engine.explainSupported) { Some(a(cls := "explain-view-link", href := "#")("Explain")) } else { None },
    if (engine.analyzeSupported) { Some(a(cls := "analyze-view-link", href := "#")("Analyze")) } else { None },
    Some(a(cls := "right definition-link initially-hidden", href := "#")("Definition")),
    Some(a(cls := "right columns-link initially-hidden", href := "#")("Columns"))
  ).flatten

  def forView(engine: DatabaseEngine, queryId: UUID, tableName: String) = {
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
            div(cls := "card-action")(linksFor(engine))
          )
        )
      ),
      div(id := s"workspace-$queryId")
    )
  }
}
