package models.template

import java.util.UUID

import scalatags.Text.all._

object ProcedureDetailTemplate {
  def forProcedure(queryId: UUID, procedureName: String) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "card")(
            div(cls := "card-content")(
              span(cls := "card-title")(
                i(cls := "title-icon fa fa-code"),
                procedureName,
                i(cls := "right fa fa-close")
              )
            ),
            div(cls := "card-action")()
          )
        )
      ),
      div(id := s"workspace-$queryId")
    )
  }
}
