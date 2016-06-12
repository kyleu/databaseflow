package services.user

import java.util.UUID

import akka.actor.ActorRef
import models.{GetQueryHistory, QueryHistoryResponse}
import models.user.User

object QueryHistoryService {
  def handleGetQueryHistory(connectionId: UUID, user: Option[User], gqh: GetQueryHistory, out: ActorRef) = {
    val msg = QueryHistoryResponse(Seq.empty)
    out ! msg
  }
}
