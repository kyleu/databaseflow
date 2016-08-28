package models.charting.options

case object BarChartOptions extends ChartOptions {
  override val selects = Seq("x" -> "X", "y" -> "Y", "color" -> "Color", "text" -> "Text")
  override val flags = Seq(
    ("horizontal", "Horizontal", false),
    ("stacked", "Stacked", false),
    ("legend", "Legend", false)
  )
}
