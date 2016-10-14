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
  type i18n = (String, Seq[Any]) => String

  def props(id: Option[UUID], supervisor: ActorRef, connectionId: UUID, user: User, out: ActorRef, sourceAddress: String, messages: i18n) = {
    Props(SocketService(id.getOrElse(UUID.randomUUID), supervisor, connectionId, user, out, sourceAddress, messages))
  }
}

case class SocketService(
    id: UUID, supervisor: ActorRef, connectionId: UUID, user: User, out: ActorRef, sourceAddress: String, messages: SocketService.i18n
) extends InstrumentedActor with StartHelper with RequestMessageHelper with TransactionHelper with RowDataHelper with DataHelper with DetailHelper {

  protected[this] val db = attemptConnect()

  protected[this] var schema: Option[Schema] = None
  protected[this] var savedQueries: Option[Seq[SavedQuery]] = None

  protected[this] var pendingDebugChannel: Option[ActorRef] = None

  AuditRecordService.create(AuditType.Connect, user.id, Some(connectionId))

  override def preStart() = onStart()

  override def postStop() = {
    activeTransaction.foreach(_.rollback())
    if (MasterDatabase.isOpen) {
      AuditRecordService.create(AuditType.Disconnect, user.id, Some(connectionId))
    }
    supervisor ! SocketStopped(id)
  }
}
