package models.charting

import scala.scalajs.js
import scala.scalajs.js.JSON

case class ChartSettings(
    t: ChartType = ChartType.Line,
    selects: Map[String, String] = Map.empty,
    flags: Map[String, Boolean] = Map.empty
) {
  def merge(s: ChartSettings) = this.copy(selects = selects ++ s.selects, flags = flags ++ s.flags)
  lazy val asJs = js.Object(Seq(
    "t" -> t.id,
    "selects" -> js.Object(selects.toSeq),
    "flags" -> js.Object(flags.toSeq)
  ))
  lazy val asJsStr = JSON.stringify(asJs)
}

object ChartSettings {
  def fromJs(options: js.Dynamic) = {
    val t = ChartType.withNameOption(options.t.toString) match {
      case Some(x) => x
      case None => ChartType.Line
    }
    ChartSettings(t = t)
  }
}
