package ui.query

import java.util.UUID

import models.template.query.QueryParametersTemplate
import org.scalajs.jquery.{JQuery, jQuery => $}
import utils.TemplateUtils

object ParameterManager {
  private[this] var activeParams = Map.empty[UUID, Seq[(String, String, String)]]

  def onChange(queryId: UUID, sql: String, paramValues: Map[String, String]) = {
    val keys = getKeys(sql)
    val hasChanged = activeParams.get(queryId) match {
      case Some(params) => (params.size != keys.size) || (!params.zip(keys).forall(x => x._1._1 == x._2._1 && x._1._2 == x._2._2))
      case None =>
        activeParams += queryId -> keys.map(k => (k._1, k._2, paramValues.getOrElse(k._1, "")))
        true
    }
    if (hasChanged) {
      val panel = $(s"#panel-$queryId .sql-parameters")
      render(queryId, keys, panel)
      TemplateUtils.changeHandler($("input", panel), jq => {
        val k = jq.data("key").toString
        val t = jq.data("t").toString
        val v = jq.value().toString
        val orig = activeParams(queryId)
        val merged = orig.filterNot(_._1 == k) :+ ((k, t, v))
        activeParams += queryId -> merged
      })
    }
  }

  def getParamsOpt(queryId: UUID) = activeParams.get(queryId).map { x =>
    x.map(r => r._1 -> r._3).toMap
  }

  def getParams(sql: String, queryId: UUID) = sql -> getParamsOpt(queryId).getOrElse(Map.empty)

  def remove(queryId: UUID) = activeParams = activeParams - queryId

  private[this] def getKeys(sql: String) = {
    var startIndex = -1
    sql.zipWithIndex.foldLeft(Seq.empty[(String, String)])((x, y) => y match {
      case ('{', idx) =>
        startIndex = idx
        x
      case ('}', idx) =>
        val v = sql.substring(startIndex + 1, idx)
        val ret = v.indexOf(':') match {
          case -1 => v -> "string"
          case i =>
            val split = v.split(':')
            split.headOption.getOrElse(throw new IllegalStateException()).trim -> split.tail.mkString(":").trim
        }
        if (!x.exists(_._1 == ret._1)) {
          x :+ ret
        } else {
          x
        }
      case _ => x
    })
  }

  private[this] def render(queryId: UUID, keys: Seq[(String, String)], panel: JQuery) = {
    if (panel.length != 1) {
      throw new IllegalStateException(s"Encountered [${panel.length}] parameter panels.")
    }
    if (keys.isEmpty) {
      activeParams += queryId -> Nil
      panel.hide()
    } else {
      val params = activeParams(queryId)
      val values = keys.map(k => (k._1, k._2, params.find(_._1 == k._1).map(_._3).getOrElse("")))
      activeParams += queryId -> values
      panel.html(QueryParametersTemplate.forValues(queryId, values).toString)
      panel.show()
    }
  }
}
