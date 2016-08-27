package models.charting.options

case object BubbleChart3DOptions extends ChartOptions {
  override val selects = Seq("x" -> "X", "y" -> "Y", "z" -> "Z", "color" -> "Color", "hover" -> "Text", "size" -> "Size")
  override val flags = Seq(("legend", "Legend", false))
}
