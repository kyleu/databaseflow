package models.templates

import models.schema.Table
import scalatags.Text.all._

object SchemaTemplate {
  def forTables(s: Seq[Table]) = {
    div(cls := "schema")(s.map(forTable))
  }

  def forTable(t: Table) = {
    div(t.name)
  }
}
