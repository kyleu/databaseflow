package ui.query

import java.util.UUID

import models.query.SavedQuery
import models.template.query.QueryParametersTemplate
import org.scalajs.jquery.{JQuery, jQuery => $}
import utils.TemplateUtils

object ParameterManager extends ParameterChangeManager {
  def onChange(queryId: UUID, sql: String, forceRefresh: Boolean = false) = {
    val keys = getKeys(sql)
    val hasChanged = forceRefresh || (activeParams.get(queryId) match {
      case Some(params) => (params.size != keys.size) || (!params.zip(keys).forall(x => x._1._1 == x._2._1 && x._1._2 == x._2._2))
      case None => throw new IllegalStateException(s"Cache not initialized for query [$queryId].")
    })
    if (hasChanged) {
      val panel = $(s"#panel-$queryId .sql-parameters")
      render(queryId, keys, panel)
      TemplateUtils.changeHandler($("input", panel), jq => {
        val k = jq.data("key").toString
        val t = jq.data("t").toString
        val v = jq.value().toString
        val orig = activeParams(queryId)
        val merged = orig.filterNot(_._1 == k) :+ ((k, t, v))
        utils.Logging.info(s"Orig: $orig / Merged: $merged")
        set(queryId, merged)
        val mergedSql = merge(sql, merged.map(x => SavedQuery.Param(x._1, x._3)))
        QueryCheckManager.check(queryId, mergedSql)
      })
    }
  }

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

  private[this] def render(queryId: UUID, keys: Seq[(String, String)], panel: JQuery) = {
    //utils.Logging.info("Render Keys: " + keys.mkString(", "))
    if (panel.length != 1) { throw new IllegalStateException(s"Encountered [${panel.length}] parameter panels.") }
    if (keys.isEmpty) {
      set(queryId, Nil)
      panel.hide()
    } else {
      val params = activeParams(queryId)
      val values = keys.map(k => (k._1, k._2, params.find(_._1 == k._1).map(_._3).getOrElse("")))
      set(queryId, values)
      panel.html(QueryParametersTemplate.forValues(queryId, values).toString)
      panel.show()
    }
  }
}
