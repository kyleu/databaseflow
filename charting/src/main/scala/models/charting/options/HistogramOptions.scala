package models.charting.options

case class HistogramOptions(
  override val columns: Seq[String] = Seq("a"),
  horizontal: Boolean = false,
  legend: Boolean = false
) extends ChartOptions
