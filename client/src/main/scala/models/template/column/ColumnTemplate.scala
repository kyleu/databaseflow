package models.template.column

import models.schema.{Column, ColumnDetails}

import scalatags.Text.all._

object ColumnTemplate {
  def linkFor(col: Column) = a(
    href := "?",
    cls := "theme-text column-detail-link",
    data("col") := col.name,
    data("t") := col.columnType.toString
  )(col.name)

  def columnDetailsPanel(owner: String, name: String, t: String) = div(
    h5(owner + " :: " + name),
    em(t + " column"),
    hr(),
    div(cls := "stats-panel")(
      div("Loading statistics... (", a(href := "", cls := "theme-text column-detail-cancel-link")("cancel"), ")")
    )
  )

  def columnDetails(details: ColumnDetails) = {
    val rows = Seq(
      Some(tr(td("Count"), td(utils.NumberUtils.withCommas(details.count)))),
      Some(tr(td("Distinct Values"), td(utils.NumberUtils.withCommas(details.distinctCount)))),
      details.error.map(err => tr(td("Error"), td(cls := "column-details-error")(err)))
    ).flatten
    table(cls := "bordered highlight")(
      tbody(rows: _*)
    )
  }
}
