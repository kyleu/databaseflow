package models.template

import utils.Messages

import scalatags.Text.all._

object GraphQLTemplate {
  val panel = {
    val content = div(id := "graphql-panel")(
      div(cls := "graphql-content")(
        iframe(style := "height: 600px; width: 100%; border: none;")
      )
    )

    StaticPanelTemplate.row(StaticPanelTemplate.panel(
      content = content,
      iconAndTitle = Some(Icons.graphQL -> span(Messages("graphql.title")))
    ))
  }

  def content() = {
    p("GraphQL kinda works!")
  }
}
