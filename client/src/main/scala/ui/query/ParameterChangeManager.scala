package ui.query

import java.util.UUID

import models.query.SavedQuery

trait ParameterChangeManager {
  protected[this] var activeParams = Map.empty[UUID, Seq[(String, String, String)]]

  protected[this] def set(queryId: UUID, v: Seq[(String, String, String)]) = activeParams += queryId -> v

  def setValues(queryId: UUID, paramValues: Seq[SavedQuery.Param]) = {
    val newParams = activeParams.get(queryId) match {
      case Some(params) => params.map { v =>
        (v._1, v._2, paramValues.find(_.k == v._1).map(_.v).getOrElse(v._3))
      } ++ paramValues.filterNot(x => params.exists(_._1 == x.k)).map(x => (x.k, "string", x.v))
      case None => paramValues.map(x => (x.k, "string", x.v))
    }
    set(queryId, newParams)
  }

  def getParamsOpt(queryId: UUID) = activeParams.get(queryId).map { x =>
    x.map(r => SavedQuery.Param(r._1, r._3))
  }

  def getParams(sql: String, queryId: UUID) = sql -> getParamsOpt(queryId).getOrElse(Seq.empty)

  def remove(queryId: UUID) = activeParams = activeParams - queryId

  protected[this] def getKeys(sql: String) = {
    var startIndex = -1
    sql.zipWithIndex.foldLeft(Seq.empty[(String, String)])((x, y) => y match {
      case ('{', idx) =>
        startIndex = idx
        x
      case ('}', idx) if idx == (startIndex + 1) => x
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
}
