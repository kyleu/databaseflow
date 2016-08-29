package models.charting

import scala.scalajs.js

case class ChartSettings(
    t: ChartType = ChartType.Line,
    selects: Map[String, String] = Map.empty,
    flags: Map[String, Boolean] = Map.empty
) {
  def merge(s: ChartSettings) = this.copy(selects = selects ++ s.selects, flags = flags ++ s.flags)

  lazy val asJson = s"""{
  |  "t": "",
  |  "selects": {
  |    ${selects.map(s => s""""${s._1}": "${s._2}"""").mkString("\n    ")}
  |  },
  |  "flags": {
  |    ${flags.map(s => s""""${s._1}": ${s._2}""").mkString("\n    ")}
  |  }""".stripMargin
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
