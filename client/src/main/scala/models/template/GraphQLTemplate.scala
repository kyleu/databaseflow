package models.template

import models.audit.AuditRecord
import utils.{Messages, TemplateUtils}

import scalatags.Text.all._

object GraphQLTemplate {
  val panel = {
    val content = div(id := "graphql-panel")(
      div(cls := "graphql-content")(Messages("general.loading"))
    )

    StaticPanelTemplate.row(StaticPanelTemplate.panel(
      content = content,
      iconAndTitle = Some(Icons.history -> span(Messages("graphql.title")))
    ))
  }

  def content() = {
    p("GraphQL kinda works!")
  }
}
