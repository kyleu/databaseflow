package services.user

import java.util.UUID

import akka.actor.ActorRef
import models.queries.audit.AuditRecordQueries
import models.{GetQueryHistory, QueryHistoryResponse}
import models.user.User
import services.database.MasterDatabase

object QueryHistoryService {
  def handleGetQueryHistory(connectionId: UUID, user: Option[User], gqh: GetQueryHistory, out: ActorRef) = {
    val matching = MasterDatabase.conn.query(AuditRecordQueries.GetMatching(connectionId, user.map(_.id), gqh.limit, gqh.offset))
    val msg = QueryHistoryResponse(matching)
    out ! msg
  }
}
