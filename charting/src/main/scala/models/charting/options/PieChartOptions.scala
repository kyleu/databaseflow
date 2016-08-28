package models.charting.options

case object PieChartOptions extends ChartOptions {
  override val selects = Seq("values" -> "Values", "labels" -> "Text")
  override val flags = Seq(
    ("showLabel", "Label", false),
    ("showValue", "Value", false),
    ("showPercentage", "Percentage", true),
    ("sorted", "Sorted", true)
  )
}
