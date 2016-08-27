package models.charting.options

case class BubbleChart3DOptions(
  override val columns: Seq[String] = Seq("x", "y", "z", "color", "hover", "size"),
  legend: Boolean = false
) extends ChartOptions
