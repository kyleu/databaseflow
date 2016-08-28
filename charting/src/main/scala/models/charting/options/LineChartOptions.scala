package models.charting.options

case object LineChartOptions extends ChartOptions {
  override val selects = Seq("x" -> "X", "y" -> "Y", "text" -> "Text")
  override val flags = Seq(
    ("smoothed", "Smoothed", false),
    ("showPoints", "Points", false)
  )
}
