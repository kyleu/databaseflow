package models.charting.options

case object BubbleChartOptions extends ChartOptions {
  override val selects = Seq("x" -> "X", "y" -> "Y", "color" -> "Color", "hover" -> "Text", "size" -> "Size")
  override val flags = Seq(("legend", "Legend", false))
}
