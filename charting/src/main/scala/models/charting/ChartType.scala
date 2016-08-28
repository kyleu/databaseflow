package models.charting

import enumeratum._

sealed abstract class ChartType(
    val id: String,
    val title: String,
    val selects: Seq[(String, String)],
    val flags: Seq[(String, String, Boolean)],
    val canCombine: Boolean = true
) extends EnumEntry {
  lazy val defaultSettings = ChartSettings(t = this, flags = flags.map(f => f._1 -> f._3).toMap)
  override val toString = id
}

object ChartType extends Enum[ChartType] {
  case object Line extends ChartType("line", "Line Chart", selects = Seq("x" -> "X", "y" -> "Y", "text" -> "Text"), flags = Seq(
    ("smoothed", "Smoothed", false),
    ("showPoints", "Points", false),
    ("legend", "Legend", false)
  ))
  case object Bar extends ChartType("bar", "Bar Chart", selects = Seq("x" -> "X", "y" -> "Y", "color" -> "Color", "hover" -> "Text"), flags = Seq(
    ("horizontal", "Horizontal", false),
    ("stacked", "Stacked", false),
    ("legend", "Legend", false)
  ))
  case object Pie extends ChartType("pie", "Pie Chart", selects = Seq("values" -> "Values", "labels" -> "Text"), flags = Seq(
    ("showLabel", "Label", false),
    ("showValue", "Value", false),
    ("showPercentage", "Percentage", true),
    ("sorted", "Sorted", true)
  ))
  case object Scatter extends ChartType("scatter", "Scatter Plot", selects = Seq("x" -> "X", "y" -> "Y", "color" -> "Color", "hover" -> "Text"), flags = Seq(
    ("legend", "Legend", false)
  ))
  case object Bubble extends ChartType(
    "bubble",
    "Bubble Chart",
    selects = Seq("x" -> "X", "y" -> "Y", "color" -> "Color", "hover" -> "Text", "size" -> "Size"),
    flags = Seq(("legend", "Legend", false))
  )
  case object Histogram extends ChartType("histogram", "Histogram", selects = Seq("y" -> "Y"), flags = Seq(
    ("horizontal", "Horizontal", false),
    ("legend", "Legend", false)
  ))
  case object Box extends ChartType("box", "Box Plot", selects = Seq("values" -> "Values", "x" -> "X"), flags = Seq(
    ("statistics", "Statistics", false),
    ("horizontal", "Horizontal", false),
    ("legend", "Legend", false)
  ))
  case object Scatter3D extends ChartType(
    "scatter3d",
    "3D Scatter Plot",
    selects = Seq("x" -> "X", "y" -> "Y", "z" -> "Z", "color" -> "Color", "hover" -> "Text"),
    flags = Seq(("legend", "Legend", false))
  )
  case object Bubble3D extends ChartType(
    "bubble3d",
    "3D Bubble Chart",
    selects = Seq("x" -> "X", "y" -> "Y", "z" -> "Z", "color" -> "Color", "hover" -> "Text", "size" -> "Size"),
    flags = Seq(("legend", "Legend", false))
  )

  override val values = findValues
}
