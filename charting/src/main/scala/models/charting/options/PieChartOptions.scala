package models.charting.options

case class PieChartOptions(
  override val columns: Seq[String] = Seq("values", "labels"),
  showLabel: Boolean = false,
  showValue: Boolean = false,
  showPercentage: Boolean = false,
  sorted: Boolean = true
) extends ChartOptions
