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

  def cardRow(title: String, content: TypedTag[String], icon: Option[String] = None, actions: Seq[TypedTag[String]] = Nil, showClose: Boolean = true) = {
    val cardContent = div(cls := "card-content")(
      span(cls := "card-title")(Seq(
        icon.map(icn => i(cls := "title-icon fa " + icn)),
        Some(span(cls := "title")(title)),
        if (showClose) { Some(i(cls := s"right fa ${Icons.close}")) } else { None }
      ).flatten: _*),
      div(cls := "content")(content)
    )

    div(cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(
          cardContent,
          if (actions.isEmpty) {
            div(cls := "card-action initially-hidden")()
          } else {
            div(cls := "card-action")(actions)
          }
        )
      )
    )
  }
}
