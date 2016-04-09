package models.template

import java.util.UUID

import models.schema.Table

import scalatags.Text.all._

object ViewDefinitionTemplate {
  def definitionForView(resultId: UUID, queryId: UUID, v: Table) = {
    div(id := resultId.toString, cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(
          div(cls := "card-content")(
            span(cls := "card-title")(
              i(cls := s"title-icon fa ${Icons.definition}"),
              "View Definition for ",
              em(v.name),
              i(cls := s"right fa ${Icons.close}")
            ),
            v.definition match {
              case Some(definition) => pre(cls := "pre-wrap")(definition)
              case None => div("No definition is available for this view.")
            }
          )
        )
      )
    )
  }
}
