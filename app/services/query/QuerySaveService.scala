package services.query

import java.util.UUID

import akka.actor.ActorRef
import models._
import models.audit.AuditType
import models.query.SavedQuery
import models.user.User
import services.audit.AuditRecordService
import utils.Logging

import scala.util.control.NonFatal

object QuerySaveService extends Logging {
  def handleQuerySaveRequest(user: Option[User], sq: SavedQuery, out: ActorRef) = {
    log.info(s"Saving query as [${sq.id}].")
    try {
      val result = SavedQueryService.save(sq, user.map(_.id))
      AuditRecordService.create(AuditType.SaveQuery, user.map(_.id), None, Some(result.id.toString))
      out ! QuerySaveResponse(savedQuery = result)
    } catch {
      case NonFatal(x) => out ! QuerySaveResponse(error = Some(x.getMessage), savedQuery = sq)
    }
  }

  def handleQueryDeleteRequest(user: Option[User], id: UUID, out: ActorRef) = {
    log.info(s"Deleting query [$id].")
    try {
      SavedQueryService.delete(id, user.map(_.id))
      AuditRecordService.create(AuditType.DeleteQuery, user.map(_.id), None, Some(id.toString))
      out ! QueryDeleteResponse(id = id)
    } catch {
      case NonFatal(x) => out ! QueryDeleteResponse(id = id, error = Some(x.getMessage))
    }
  }
}
