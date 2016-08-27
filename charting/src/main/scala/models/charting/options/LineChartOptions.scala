package models.charting.options

case class LineChartOptions(
  override val columns: Seq[String] = Seq("x", "y", "hover"),
  smoothed: Boolean = false,
  showPoints: Boolean = false,
  legend: Boolean = false
) extends ChartOptions
