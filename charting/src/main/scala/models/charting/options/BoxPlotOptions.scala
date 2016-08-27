package models.charting.options

case class BoxPlotOptions(
  override val columns: Seq[String] = Seq("values", "x"),
  statistics: Boolean = false,
  horizontal: Boolean = false,
  legend: Boolean = false
) extends ChartOptions
