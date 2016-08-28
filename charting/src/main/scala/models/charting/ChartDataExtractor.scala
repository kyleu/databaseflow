package models.charting

import scala.scalajs.js

object ChartDataExtractor {
  private[this] def getColumn(key: String, settings: ChartSettings, columns: Seq[(String, String)], data: js.Array[js.Array[String]]) = {
    settings.selects.get(key) match {
      case Some(x) =>
        val col = settings.selects.getOrElse(key, "")
        val idx = columns.indexWhere(_._1 == col)
        val ret = if (idx == -1) { js.Array() } else { data.map(_(idx)) }
        ret
      case None => js.Array()
    }
  }

  def getJsData(settings: ChartSettings, columns: Seq[(String, String)], data: js.Array[js.Array[String]]) = settings.t match {
    case ChartType.Line => Seq(
      js.Dynamic.literal(
        "x" -> getColumn("x", settings, columns, data),
        "y" -> getColumn("y", settings, columns, data),
        "text" -> getColumn("text", settings, columns, data),
        "line" -> (if (settings.flags.getOrElse("smoothed", false)) {
          js.Dynamic.literal("shape" -> "spline")
        } else {
          js.Dynamic.literal("shape" -> "linear")
        }),
        "mode" -> (if (settings.flags.getOrElse("showPoints", false)) {
          "lines+markers"
        } else {
          "lines"
        })
      )
    )
    case x => throw new IllegalArgumentException(x.id)
  }
}
