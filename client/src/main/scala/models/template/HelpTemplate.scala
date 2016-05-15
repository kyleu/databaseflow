package models.template

import scalatags.Text.all._

object HelpTemplate {
  def content() = {
    val content = div(id := "help-panel")(
      "Help!"
    )

    StaticPanelTemplate.cardRow(
      content = content,
      iconAndTitle = Some(Icons.help -> "Database Flow Help")
    )
  }
}
