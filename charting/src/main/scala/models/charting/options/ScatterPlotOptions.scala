package models.charting.options

case object ScatterPlotOptions extends ChartOptions {
  override val selects = Seq("x" -> "X", "y" -> "Y", "color" -> "Color", "hover" -> "Text")
  override val flags = Seq(("legend", "Legend", false))
}
