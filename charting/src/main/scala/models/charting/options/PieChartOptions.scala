package models.charting.options

import models.charting.ChartSettings

import scala.scalajs.js

case object PieChartOptions extends ChartOptions {
  override val selects = Seq("values" -> "Values", "labels" -> "Text")
  override val flags = Seq(
    ("showLabel", "Label", false),
    ("showValue", "Value", false),
    ("showPercentage", "Percentage", true),
    ("sorted", "Sorted", true)
  )

  override def getJsData(settings: ChartSettings, columns: Seq[(String, String)], data: js.Array[js.Array[String]]) = Seq(
    js.Dynamic.literal(
      "values" -> getDataColumn("values", settings, columns, data),
      "labels" -> getDataColumn("labels", settings, columns, data),
      "type" -> "pie"
    )
  )

  override def getJsOptions(settings: ChartSettings) = js.Dynamic.literal()
}
