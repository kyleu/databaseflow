package models.template

import java.util.UUID

import scalatags.Text.all._

object SqlEditorTemplate {
  def forQuery(queryId: UUID) = {
    div(id := s"panel-$queryId")(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "card")(
            div(cls := "card-content")(
              span(cls := "card-title")("Untitled Query"),
              div(id := "sql-textarea", style := "width: 100%;")("select * from actor limit 5;")
            ),
            div(cls := "card-action")(
              a(id := "run-query-link", href := "#")("Run"),
              a(id := "explain-query-link", href := "#")("Explain"),
              a(id := "analyze-query-link", href := "#")("Analyze"),
              a(id := "save-query-link", cls := "right", href := "#")("Save"),
              a(id := "load-query-link", cls := "right", href := "#")("Load")
            )
          )
        )
      ),
      div(id := "workspace")
    )

  }
}
