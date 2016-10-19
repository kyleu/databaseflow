package models.template

import scalatags.Text.TypedTag
import scalatags.Text.all._

object StaticPanelTemplate {
  def row(t: TypedTag[String]) = div(cls := "row")(div(cls := "col s12")(t))

  def card(
    content: TypedTag[String],
    iconAndTitle: Option[(String, TypedTag[String])] = None,
    actions: Seq[TypedTag[String]] = Nil,
    showClose: Boolean = true
  ) = {
    val closeEl = if (showClose) {
      Some(i(cls := s"right theme-text fa ${Icons.close}"))
    } else {
      None
    }

    val titleEl = iconAndTitle.map { it =>
      div(cls := "card-title")(
        i(cls := s"title-icon fa ${it._1} theme-text"),
        span(cls := "title")(it._2)
      )
    }

    val cardContent = div(cls := "card-content")(Seq(
      closeEl,
      titleEl,
      Some(div(cls := "content")(content))
    ).flatten: _*)

    div(cls := "card")(if (actions.isEmpty) {
      Seq(cardContent)
    } else {
      Seq(cardContent, div(cls := "card-action")(actions))
    })
  }

  def panel(
    content: TypedTag[String],
    iconAndTitle: Option[(String, TypedTag[String])] = None,
    actions: Seq[TypedTag[String]] = Nil,
    showClose: Boolean = true
  ) = {
    val closeEl = if (showClose) {
      Some(i(cls := s"right theme-text fa ${Icons.close}"))
    } else {
      None
    }

    val titleEl = iconAndTitle.map { it =>
      div(cls := "panel-title")(
        i(cls := s"title-icon fa ${it._1} theme-text"),
        span(cls := "title")(it._2)
      )
    }

    val panelContent = div(cls := "panel-content")(Seq(
      closeEl,
      titleEl,
      Some(div(cls := "content")(content))
    ).flatten: _*)

    div(cls := "content-panel")(if (actions.isEmpty) {
      Seq(panelContent)
    } else {
      Seq(panelContent, div(cls := "panel-action")(actions))
    })
  }
}
