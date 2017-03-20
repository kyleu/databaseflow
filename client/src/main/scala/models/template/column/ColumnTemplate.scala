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

  def columnDetails(modelType: String, modelName: String, name: String, t: String) = div(
    "Column details!"
  )
}
