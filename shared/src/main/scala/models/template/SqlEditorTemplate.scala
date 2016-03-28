package models.template

import java.util.UUID

import scalatags.Text.all._

object SqlEditorTemplate {
  def forQuery(queryId: UUID, queryName: String, sql: String, icon: String) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "card")(
            div(cls := "card-content")(
              span(cls := "card-title")(
                i(cls := "title-icon fa " + icon),
                queryName,
                i(cls := s"right fa ${Icons.close}")
              ),
              div(id := s"sql-textarea-$queryId", cls := "sql-textarea", style := "width: 100%;")(sql)
            ),
            div(cls := "card-action")(
              a(cls := "run-query-link", href := "#")("Run"),
              a(cls := "explain-query-link", href := "#")("Explain"),
              a(cls := "analyze-query-link", href := "#")("Analyze"),
              a(cls := "save-query-link right", href := "#")("Save"),
              a(cls := "save-query-link right", href := "#")("Clone")
            )
          )
        )
      ),
      div(id := s"workspace-$queryId")
    )
  }
}
