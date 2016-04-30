package models.template

import java.util.UUID

import scalatags.Text.TypedTag
import scalatags.Text.all._

object StaticPanelTemplate {
  def panel(queryId: UUID, title: String, content: TypedTag[String], icon: String) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      cardRow(content, Some(icon -> title)),
      div(id := s"workspace-$queryId")
    )
  }

  def cardRow(content: TypedTag[String], iconAndTitle: Option[(String, String)] = None, actions: Seq[TypedTag[String]] = Nil, showClose: Boolean = true) = {
    val closeEl = if (showClose) {
      Some(i(cls := s"right fa ${Icons.close}"))
    } else {
      None
    }

    val titleEl = iconAndTitle.map { it =>
      div(cls := "card-title")(
        i(cls := "title-icon fa " + it._1),
        span(cls := "title")(it._2)
      )
    }

    val cardContent = div(cls := "card-content")(Seq(
      closeEl,
      titleEl,
      Some(div(cls := "content")(content))
    ).flatten: _*)

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
