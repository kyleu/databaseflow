package ui.query

import java.util.UUID

import models.CheckQuery
import org.scalajs.dom
import utils.NetworkMessage

object QueryCheckManager {
  private[this] var sqlChecks = Map.empty[UUID, String]

  def isChanged(queryId: UUID, s: String) = !sqlChecks.get(queryId).contains(s)

  def check(queryId: UUID, sql: String) = {
    dom.window.setTimeout(() => {
      val currentSql = SqlManager.getSql(queryId)
      val params = ParameterManager.getParams(currentSql, queryId)._2
      val merged = ParameterManager.merge(currentSql, params)
      if (merged == sql) {
        sqlChecks += (queryId -> merged)
        NetworkMessage.sendMessage(CheckQuery(queryId, merged))
      }
    }, 2000)
  }

  def remove(queryId: UUID) = sqlChecks -= queryId
}
