package models.charting.options

case object HistogramOptions extends ChartOptions {
  override val selects = Seq("y" -> "Y")
  override val flags = Seq(
    ("horizontal", "Horizontal", false),
    ("legend", "Legend", false)
  )
}
