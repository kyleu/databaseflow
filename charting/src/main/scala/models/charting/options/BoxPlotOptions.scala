package models.charting.options

import models.charting.ChartSettings

import scala.scalajs.js

case object BoxPlotOptions extends ChartOptions {
  override val selects = Seq("x" -> "X", "y" -> "Y")
  override val flags = Seq(
    ("statistics", "Statistics", false),
    ("horizontal", "Horizontal", false)
  )

  override def getJsData(settings: ChartSettings, columns: Seq[(String, String)], data: js.Array[js.Array[String]]) = Seq(
    js.Dynamic.literal(
      "x" -> getDataColumn("x", settings, columns, data),
      "y" -> getDataColumn("y", settings, columns, data),
      "type" -> "box"
    )
  )

  override def getJsOptions(settings: ChartSettings) = js.Dynamic.literal()

}
