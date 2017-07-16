package services.query

import models.query.SavedQuery
import util.Logging

object ParameterService extends Logging {
  def merge(sql: String, params: Seq[SavedQuery.Param]) = {
    var merged = sql
    params.foreach { param =>
      if (param.v.trim.nonEmpty) {
        var idx = Math.max(merged.indexOf("{" + param.k + ":"), merged.indexOf("{" + param.k + "}"))
        while (idx > -1) {
          val end = merged.indexOf('}', idx) + 1
          merged = merged.replaceAllLiterally(merged.substring(idx, end), param.v)
          idx = Math.max(merged.indexOf("{" + param.k + ":"), merged.indexOf("{" + param.k + "}"))
        }
      }
    }
    merged
  }
}
