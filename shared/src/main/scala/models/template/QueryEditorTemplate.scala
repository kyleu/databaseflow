package models.template

import java.util.UUID

import scalatags.Text.TypedTag
import scalatags.Text.all._

object QueryEditorTemplate {
  def forAdHocQuery(queryId: UUID, queryName: String, sql: String) = {
    val links = Seq(
      a(cls := "save-query-link right", href := "#")("Save")
    )
    queryPanel(queryId, queryName, sql, Icons.adHocQuery, links)
  }

  def forSavedQuery(queryId: UUID, queryName: String, sql: String) = {
    val links = Seq(
      a(cls := "save-query-link right", href := "#")("Save"),
      a(cls := "save-as-query-link right", href := "#")("Save As New"),
      a(cls := "delete-query-link right", href := "#")("Delete")
    )
    queryPanel(queryId, queryName, sql, Icons.savedQuery, links)
  }

  private[this] def queryPanel(queryId: UUID, queryName: String, sql: String, icon: String, links: Seq[TypedTag[String]]) = {
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
            div(cls := "card-action")(Seq(
              a(cls := "run-query-link", href := "#")("Run"),
              a(cls := "explain-query-link", href := "#")("Explain"),
              a(cls := "analyze-query-link", href := "#")("Analyze")
            ) ++ links: _*)
          )
        )
      ),
      div(id := s"workspace-$queryId")
    )
  }
}
