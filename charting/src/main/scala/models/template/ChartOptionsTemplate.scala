package models.template

import scala.scalajs.js

import scalatags.Text.all._

object ChartOptionsTemplate {
  def columnSelect(cssClass: String, columns: Seq[(String, String)], activeOption: Option[String]) = select(cls := cssClass)(
    columns.map { col =>
      if (activeOption.contains(col._1)) {
        option(value := col._1, selected)(s"${col._1} (${col._2})")
      } else {
        option(value := col._1)(s"${col._1} (${col._2})")
      }
    }
  )

  def forChart(columns: Seq[(String, String)], chart: js.Dynamic) = div(cls := "row")(
    div(cls := "input-field col s12")(
      columnSelect("chart-type-select", models.charting.ChartType.values.map(t => t.title -> t.id), None),
      label("Chart Type")
    ),
    div(cls := "input-field col s12")(
      columnSelect("chart-column-select", columns, None),
      label("Column List")
    )
  )
}
