package models.template

import java.util.UUID

import scalatags.Text.TypedTag
import scalatags.Text.all._

object StaticPanelTemplate {
  def panel(queryId: UUID, title: String, content: TypedTag[String], icon: String) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      cardRow(title, content, Some(icon)),
      div(id := s"workspace-$queryId")
    )
  }

  def cardRow(title: String, content: TypedTag[String], icon: Option[String] = None, actions: Option[Seq[TypedTag[String]]] = None) = {
    val cardContent = div(cls := "card-content")(
      span(cls := "card-title")(Seq(
        icon.map(icn => i(cls := "title-icon fa " + icn)),
        Some(span(title)),
        Some(i(cls := s"right fa ${Icons.close}"))
      ).flatten: _*),
      content
    )

    val cardActions = actions.map { x =>
      div(cls := "card-action")(x)
    }

    div(cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(Seq(
          Some(cardContent),
          cardActions
        ).flatten: _*)
      )
    )
  }
}
