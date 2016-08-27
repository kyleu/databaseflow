package models.charting

import scala.scalajs.js.Dynamic

case class ChartSettings(
  t: ChartType = ChartType.Line,
  selects: Seq[(String, String)] = Nil,
  flags: Seq[(String, Boolean)] = Nil
)

object ChartSettings {
  def fromJs(options: Dynamic) = {
    val t = ChartType.withNameOption(options.t.toString) match {
      case Some(x) => x
      case None => ChartType.Line
    }
    ChartSettings(t = t)
  }
}
