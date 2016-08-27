package models.charting.options

case class ScatterPlotOptions(
  override val columns: Seq[String] = Seq("x", "y", "color", "hover"),
  legend: Boolean = false
) extends ChartOptions
