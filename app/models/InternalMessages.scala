package models

import java.util.UUID

import akka.actor.ActorRef
import models.user.User
import org.joda.time.LocalDateTime
import play.api.libs.json.JsObject

sealed trait InternalMessage

final case class ConnectionStarted(user: User, connectionId: UUID, conn: ActorRef) extends InternalMessage
final case class ConnectionStopped(connectionId: UUID) extends InternalMessage

case object GetSystemStatus extends InternalMessage
final case class SystemStatus(connections: Seq[(UUID, String)]) extends InternalMessage

final case class SendConnectionTrace(id: UUID) extends InternalMessage
final case class ConnectionTraceResponse(id: UUID, userId: UUID, username: Option[String]) extends InternalMessage

final case class SendClientTrace(id: UUID) extends InternalMessage
final case class ClientTraceResponse(id: UUID, data: JsObject) extends InternalMessage
