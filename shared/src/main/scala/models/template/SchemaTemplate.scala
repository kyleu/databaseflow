package models.template

import models.schema.{ Schema, Table }
import scalatags.Text.all._

object SchemaTemplate {
  def forSchema(s: Schema) = {
    div(cls := "schema")(s.tables.map(forTable))
  }

  def forTable(t: Table) = {
    div(t.name)
  }
}
