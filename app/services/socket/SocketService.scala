package services.socket

import java.util.UUID

import akka.actor.{ActorRef, Props}
import models._
import models.audit.AuditType
import models.query.SavedQuery
import models.schema.Schema
import models.user.User
import services.audit.AuditRecordService
import services.database.core.MasterDatabase
import utils.metrics.InstrumentedActor

object SocketService {
  def props(id: Option[UUID], supervisor: ActorRef, connectionId: UUID, user: Option[User], out: ActorRef, sourceAddress: String) = {
    Props(SocketService(id.getOrElse(UUID.randomUUID), supervisor, connectionId, user, out, sourceAddress))
  }
}

case class SocketService(
    id: UUID, supervisor: ActorRef, connectionId: UUID, user: Option[User], out: ActorRef, sourceAddress: String
) extends InstrumentedActor with StartHelper with RequestMessageHelper with TransactionHelper with DataHelper with DetailHelper {

  protected[this] var currentUsername = user.flatMap(_.username)
  protected[this] var userPreferences = user.map(_.preferences)
  protected[this] var dbOpt = attemptConnect()
  protected[this] val db = dbOpt.getOrElse(throw new IllegalStateException("Cannot connect to database."))

  protected[this] var schema: Option[Schema] = None
  protected[this] var savedQueries: Option[Seq[SavedQuery]] = None

  protected[this] var pendingDebugChannel: Option[ActorRef] = None

  AuditRecordService.create(AuditType.Connect, user.map(_.id), Some(connectionId))

  override def preStart() = onStart()

  override def postStop() = {
    if (MasterDatabase.isOpen) {
      AuditRecordService.create(AuditType.Disconnect, user.map(_.id), Some(connectionId))
    }
    supervisor ! SocketStopped(id)
  }
}
