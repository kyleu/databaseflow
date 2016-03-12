package models

import java.util.UUID

import akka.actor.ActorRef
import models.user.User

sealed trait InternalMessage

case class ConnectionStarted(user: User, connectionId: UUID, conn: ActorRef) extends InternalMessage
case class ConnectionStopped(connectionId: UUID) extends InternalMessage

case object GetSystemStatus extends InternalMessage
case class SystemStatus(connections: Seq[(UUID, String)]) extends InternalMessage

case class SendConnectionTrace(id: UUID) extends InternalMessage
case class ConnectionTraceResponse(id: UUID, userId: UUID, username: Option[String]) extends InternalMessage

case class SendClientTrace(id: UUID) extends InternalMessage
case class ClientTraceResponse(id: UUID, data: upickle.Js.Value) extends InternalMessage
