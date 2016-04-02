package models.template

import java.util.UUID

import models.query.SavedQuery

import scalatags.Text.TypedTag
import scalatags.Text.all._

object QueryEditorTemplate {
  def forAdHocQuery(queryId: UUID, queryName: String, sql: String) = {
    val links = Seq(
      a(cls := "run-query-link", href := "#")("Run"),
      a(cls := "explain-query-link", href := "#")("Explain"),
      a(cls := "analyze-query-link", href := "#")("Analyze"),
      a(cls := "save-query-link right", href := "#")("Save")
    )
    queryPanel(queryId, queryName, None, sql, Icons.adHocQuery, links)
  }

  def forSavedQuery(sq: SavedQuery, userId: UUID) = {
    val modificationLinks = if (sq.owner.contains(userId)) {
      Seq(
        a(cls := "save-query-link right", href := "#")("Save"),
        a(cls := "save-as-query-link right", href := "#")("Save As New"),
        a(cls := "delete-query-link right", href := "#")("Delete")
      )
    } else {
      Seq(
        a(cls := "save-as-query-link right", href := "#")("Save As New")
      )
    }
    val links = Seq(
      a(cls := "run-query-link", href := "#")("Run"),
      a(cls := "explain-query-link", href := "#")("Explain"),
      a(cls := "analyze-query-link", href := "#")("Analyze")
    ) ++ modificationLinks
    queryPanel(sq.id, sq.name, sq.description, sq.sql, Icons.savedQuery, links)
  }

  def forView(queryId: UUID, viewName: String, description: Option[String], sql: String) = {
    val links = Seq(
      a(cls := "run-query-link", href := "#")("Run"),
      a(cls := "explain-query-link", href := "#")("Explain"),
      a(cls := "analyze-query-link", href := "#")("Analyze"),
      a(cls := "save-query-link right", href := "#")("Save")
    )
    queryPanel(queryId, viewName, description, sql, Icons.view, links)
  }

  private[this] def queryPanel(queryId: UUID, queryName: String, description: Option[String], sql: String, icon: String, links: Seq[TypedTag[String]]) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "card")(
            div(cls := "card-content")(
              span(cls := "card-title")(
                i(cls := "title-icon fa " + icon),
                queryName,
                span(cls := "unsaved-status", title := "Unsaved Changes")("*"),
                i(cls := s"right fa ${Icons.close}")
              ),
              div(id := s"sql-textarea-$queryId", cls := "sql-textarea", style := "width: 100%;")(sql)
            ),
            div(cls := "card-action")(links: _*)
          )
        )
      ),
      div(id := s"workspace-$queryId")
    )
  }
}
