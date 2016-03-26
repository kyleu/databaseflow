package ui

import java.util.UUID

import models.RequestMessage

import scala.util.Random

object AdHocQueryManager {
  private[this] var lastNum = 1

  def addNewQuery(sendMessage: (RequestMessage) => Unit) = {
    val queryId = UUID.randomUUID
    val queryName = if (lastNum == 1) {
      "Untitled Query"
    } else {
      "Untitled Query " + lastNum
    }
    val sql = MetadataManager.schema.map { s =>
      if (s.tables.isEmpty) { "" } else { s"select * from ${s.tables(Random.nextInt(s.tables.size)).name} limit 5;" }
    }.getOrElse("")
    QueryManager.addQuery(sendMessage, queryId, queryName, sql, () => Unit)
    lastNum += 1
  }
}
