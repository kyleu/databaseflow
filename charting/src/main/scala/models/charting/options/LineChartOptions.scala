package models.charting.options

import models.charting.ChartSettings

import scala.scalajs.js

case object LineChartOptions extends ChartOptions {
  override val selects = Seq("x" -> "X", "y" -> "Y", "text" -> "Text")

  override val flags = Seq(
    ("smoothed", "Smoothed", false),
    ("showPoints", "Points", false)
  )

  override def getJsData(settings: ChartSettings, columns: Seq[(String, String)], data: js.Array[js.Array[String]]) = Seq(
    js.Dynamic.literal(
      "x" -> getDataColumn("x", settings, columns, data),
      "y" -> getDataColumn("y", settings, columns, data),
      "text" -> getDataColumn("text", settings, columns, data),
      "line" -> (if (settings.flags.getOrElse("smoothed", false)) {
        js.Dynamic.literal("shape" -> "spline")
      } else {
        js.Dynamic.literal("shape" -> "linear")
      }),
      "mode" -> (if (settings.flags.getOrElse("showPoints", false)) {
        "lines+markers"
      } else {
        "lines"
      })
    )
  )

  override def getJsOptions(settings: ChartSettings) = js.Dynamic.literal(
    "margin" -> js.Dynamic.literal("t" -> 40),
    "xaxis" -> js.Dynamic.literal("title" -> selectValue(settings, "x")),
    "yaxis" -> js.Dynamic.literal("title" -> selectValue(settings, "y"))
  )
}
