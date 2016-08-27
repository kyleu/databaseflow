package models.charting.options

case class ScatterPlot3DOptions(
  override val columns: Seq[String] = Seq("x", "y", "z", "color", "hover"),
  legend: Boolean = false
) extends ChartOptions
