package models.charting.options

import models.charting.ChartSettings

import scala.scalajs.js

case object ScatterPlot3DOptions extends ChartOptions {
  override val selects = Seq("x" -> "X", "y" -> "Y", "z" -> "Z", "color" -> "Color", "text" -> "Text")
  override val flags = Seq()

  override def getJsData(settings: ChartSettings, columns: Seq[(String, String)], data: js.Array[js.Array[String]]) = Seq(
    js.Dynamic.literal(
      "x" -> getDataColumn("x", settings, columns, data),
      "y" -> getDataColumn("y", settings, columns, data),
      "z" -> getDataColumn("z", settings, columns, data),
      "color" -> getDataColumn("color", settings, columns, data),
      "text" -> getDataColumn("text", settings, columns, data),
      "mode" -> "markers",
      "type" -> "scatter3d"
    )
  )

  override def getJsOptions(settings: ChartSettings) = js.Dynamic.literal(
    "margin" -> js.Dynamic.literal("l" -> 0, "r" -> 0, "b" -> 0, "t" -> 0),
    "scene" -> js.Dynamic.literal(
      "xaxis" -> js.Dynamic.literal("title" -> selectValue(settings, "x")),
      "yaxis" -> js.Dynamic.literal("title" -> selectValue(settings, "y")),
      "zaxis" -> js.Dynamic.literal("title" -> selectValue(settings, "z"))
    )
  )
}
