package models.charting.options

case object BoxPlotOptions extends ChartOptions {
  override val selects = Seq("values" -> "Values", "x" -> "X")
  override val flags = Seq(
    ("statistics", "Statistics", false),
    ("horizontal", "Horizontal", false),
    ("legend", "Legend", false)
  )
}
