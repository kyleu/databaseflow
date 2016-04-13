package models.template

import java.util.UUID

import models.engine.DatabaseEngine

import scalatags.Text.all._

object ViewDetailTemplate {
  private[this] def linksFor(engine: DatabaseEngine) = Seq(
    Some(a(cls := "view-data-link", href := "#")("View Data")),
    if (engine.explain.isDefined) { Some(a(cls := "explain-view-link", href := "#")("Explain")) } else { None },
    if (engine.analyze.isDefined) { Some(a(cls := "analyze-view-link", href := "#")("Analyze")) } else { None },
    Some(a(cls := "right definition-link initially-hidden", href := "#")("Definition")),
    Some(a(cls := "right columns-link initially-hidden", href := "#")("Columns"))
  ).flatten

  def forView(engine: DatabaseEngine, queryId: UUID, tableName: String) = {
    val content = div(
      div(cls := "description")(""),
      div(cls := "summary")("")
    )

    div(id := s"panel-$queryId", cls := "workspace-panel")(
      StaticPanelTemplate.cardRow(tableName, content, icon = Some(Icons.view), actions = Some(linksFor(engine))),
      div(id := s"workspace-$queryId")
    )
  }
}
