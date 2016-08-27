package models.charting.options

case class BarChartOptions(
  override val columns: Seq[String] = Seq("x", "y", "color", "hover"),
  horizontal: Boolean = false,
  stacked: Boolean = false,
  legend: Boolean = false
) extends ChartOptions
