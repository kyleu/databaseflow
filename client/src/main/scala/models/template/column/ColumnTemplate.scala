package models.template.column

import models.schema.Column

import scalatags.Text.all._

object ColumnTemplate {
  def linkFor(col: Column) = a(
    href := "?",
    cls := "theme-text column-detail-link",
    data("col") := col.name,
    data("t") := col.columnType.toString
  )(col.name)

  def columnDetails(owner: String, name: String, t: String) = div(
    h5(owner + " :: " + name),
    em(t + " column"),
    hr(),
    div(cls := "column-detail-stats-panel")(
      div("Loading statistics... (", a(href := "", cls := "theme-text column-detail-cancel-link")("cancel"), ")")
    )
  )
}
