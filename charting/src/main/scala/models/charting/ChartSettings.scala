package models.charting

import scala.scalajs.js.Dynamic

case class ChartSettings(
    t: ChartType = ChartType.Line,
    selects: Map[String, String] = Map.empty,
    flags: Map[String, Boolean] = Map.empty
) {
  def merge(s: ChartSettings) = this.copy(selects = selects ++ s.selects, flags = flags ++ s.flags)
}

object ChartSettings {
  def fromJs(options: Dynamic) = {
    val t = ChartType.withNameOption(options.t.toString) match {
      case Some(x) => x
      case None => ChartType.Line
    }
    ChartSettings(t = t)
  }
}
