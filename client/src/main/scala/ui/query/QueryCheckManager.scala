package ui.query

import java.util.UUID

import models.CheckQuery
import org.scalajs.dom
import utils.NetworkMessage

object QueryCheckManager {

  var sqlChecks = Map.empty[UUID, String]

  def isChanged(queryId: UUID, s: String) = !sqlChecks.get(queryId).contains(s)

  def check(queryId: UUID, sql: String) = {
    dom.window.setTimeout(() => {
      val currentSql = SqlManager.getSql(queryId)
      if (currentSql == sql) {
        sqlChecks = sqlChecks + (queryId -> sql)
        NetworkMessage.sendMessage(CheckQuery(queryId, sql))
      }
    }, 2000)
  }

  def remove(queryId: UUID) = sqlChecks = sqlChecks - queryId
}
