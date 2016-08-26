package models.charting

import enumeratum._

sealed abstract class ChartType(val id: String) extends EnumEntry {
  override val toString = id
}

object ChartType extends Enum[ChartType] {
  case object Line extends ChartType("line")
  case object Bar extends ChartType("bar")
  case object Pie extends ChartType("pie")
  case object Scatter extends ChartType("scatter")
  case object Bubble extends ChartType("bubble")
  case object Scatter3D extends ChartType("scatter3d")

  override val values = findValues
}
