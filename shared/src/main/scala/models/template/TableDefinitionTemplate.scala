package models.template

import scalatags.Text.all._

object TableDefinitionTemplate {
  def definitionPanel(definition: String) = {
    pre(cls := "pre-wrap")(definition)
  }
}
