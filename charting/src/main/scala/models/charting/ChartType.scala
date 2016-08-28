package models.charting

import enumeratum._
import models.charting.options._

sealed abstract class ChartType(
    val id: String,
    val title: String,
    val options: ChartOptions,
    val canCombine: Boolean = true
) extends EnumEntry {
  lazy val defaultSettings = ChartSettings(t = this, flags = options.flags.map(f => f._1 -> f._3).toMap)
  override val toString = id
}

object ChartType extends Enum[ChartType] {
  case object Line extends ChartType("line", "Line Chart", LineChartOptions)
  case object Bar extends ChartType("bar", "Bar Chart", BarChartOptions)
  case object Pie extends ChartType("pie", "Pie Chart", PieChartOptions)
  case object Scatter extends ChartType("scatter", "Scatter Plot", ScatterPlotOptions)
  case object Bubble extends ChartType("bubble", "Bubble Chart", BubbleChartOptions)
  case object Histogram extends ChartType("histogram", "Histogram", HistogramOptions)
  case object Box extends ChartType("box", "Box Plot", BoxPlotOptions)
  case object Scatter3D extends ChartType("scatter3d", "3D Scatter Plot", ScatterPlot3DOptions)
  case object Bubble3D extends ChartType("bubble3d", "3D Bubble Chart", BubbleChart3DOptions)

  override val values = findValues
}
