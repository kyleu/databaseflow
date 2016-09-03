package services.query

import utils.Logging

object ParameterService extends Logging {
  def merge(sql: String, params: Map[String, String]) = {
    var merged = sql
    params.foreach { param =>
      if (param._2.trim.nonEmpty) {
        var idx = Math.max(merged.indexOf("{" + param._1 + ":"), merged.indexOf("{" + param._1 + "}"))
        while (idx > -1) {
          val end = merged.indexOf('}', idx) + 1
          merged = merged.replaceAllLiterally(merged.substring(idx, end), param._2)
          idx = Math.max(merged.indexOf("{" + param._1 + ":"), merged.indexOf("{" + param._1 + "}"))
        }
      }
    }
    merged
  }
}
