package models.charting.options

case object ScatterPlot3DOptions extends ChartOptions {
  override val selects = Seq("x" -> "X", "y" -> "Y", "z" -> "Z", "color" -> "Color", "hover" -> "Text")
  override val flags = Seq(("legend", "Legend", false))
}
