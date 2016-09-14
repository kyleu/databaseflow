package ui.query

import java.util.UUID

trait ParameterChangeManager {
  protected[this] var activeParams = Map.empty[UUID, Seq[(String, String, String)]]

  protected[this] def set(queryId: UUID, v: Seq[(String, String, String)]) = {
    utils.Logging.info(s"Setting values for [$queryId]: $v")
    activeParams += queryId -> v
  }

  def setValues(queryId: UUID, paramValues: Map[String, String]) = {
    val newParams = activeParams.get(queryId) match {
      case Some(params) => params.map { v =>
        (v._1, v._2, paramValues.getOrElse(v._1, v._3))
      } ++ paramValues.toSeq.filterNot(x => params.exists(_._1 == x._1)).map(x => (x._1, "string", x._2))
      case None => paramValues.toSeq.map(x => (x._1, "string", x._2))
    }
    set(queryId, newParams)
  }

  def getParamsOpt(queryId: UUID) = activeParams.get(queryId).map { x =>
    x.map(r => r._1 -> r._3).toMap
  }

  def getParams(sql: String, queryId: UUID) = sql -> getParamsOpt(queryId).getOrElse(Map.empty)

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
