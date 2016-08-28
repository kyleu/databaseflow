package models.charting.options

import models.charting.ChartSettings

import scala.scalajs.js

case object HistogramOptions extends ChartOptions {
  override val selects = Seq("x" -> "X", "text" -> "Text")
  override val flags = Seq(("horizontal", "Horizontal", false))

  override def getJsData(settings: ChartSettings, columns: Seq[(String, String)], data: js.Array[js.Array[String]]) = Seq(
    js.Dynamic.literal(
      "x" -> getDataColumn("x", settings, columns, data),
      "text" -> getDataColumn("text", settings, columns, data),
      "type" -> "histogram"
    )
  )

  override def getJsOptions(settings: ChartSettings) = js.Dynamic.literal()
}
