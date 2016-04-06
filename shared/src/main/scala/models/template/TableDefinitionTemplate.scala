package models.template

import java.util.UUID

import models.schema.Table

import scalatags.Text.all._

object TableDefinitionTemplate {
  def definitionForTable(resultId: UUID, queryId: UUID, t: Table) = {
    div(id := resultId.toString, cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(
          div(cls := "card-content")(
            span(cls := "card-title")(
              i(cls := s"title-icon fa ${Icons.definition}"),
              "Table Definition for ",
              em(t.name),
              i(cls := s"right fa ${Icons.close}")
            ),
            t.definition match {
              case Some(definition) => pre(cls := "pre-wrap")(definition)
              case None => div("No definition is available for this table.")
            }
          )
        )
      )
    )
  }
}
