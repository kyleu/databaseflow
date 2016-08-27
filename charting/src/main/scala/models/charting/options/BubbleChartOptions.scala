package models.charting.options

case class BubbleChartOptions(
  override val columns: Seq[String] = Seq("x", "y", "color", "hover", "size"),
  legend: Boolean = false
) extends ChartOptions
