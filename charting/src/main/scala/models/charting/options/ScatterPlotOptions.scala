package models.charting.options

import models.charting.ChartSettings

import scala.scalajs.js

case object ScatterPlotOptions extends ChartOptions {
  override val selects = Seq("x" -> "X", "y" -> "Y", "color" -> "Color", "text" -> "Text")
  override val flags = Seq()

  override def getJsData(settings: ChartSettings, columns: Seq[(String, String)], data: js.Array[js.Array[String]]) = Seq(
    js.Dynamic.literal(
      "x" -> getDataColumn("x", settings, columns, data),
      "y" -> getDataColumn("y", settings, columns, data),
      "color" -> getDataColumn("color", settings, columns, data),
      "text" -> getDataColumn("text", settings, columns, data),
      "mode" -> "markers",
      "type" -> "scatter"
    )
  )

  override def getJsOptions(settings: ChartSettings) = js.Dynamic.literal(
    "margin" -> js.Dynamic.literal("t" -> 40),
    "xaxis" -> js.Dynamic.literal("title" -> selectValue(settings, "x")),
    "yaxis" -> js.Dynamic.literal("title" -> selectValue(settings, "y"))
  )
}
