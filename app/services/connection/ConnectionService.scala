package services.connection

import java.util.UUID

import akka.actor.{Props, ActorRef}
import models._
import models.user.User
import utils.Config

object ConnectionService {
  def props(id: Option[UUID], supervisor: ActorRef, user: User, out: ActorRef, sourceAddress: String) = {
    Props(new ConnectionService(id.getOrElse(UUID.randomUUID), supervisor, user, out, sourceAddress))
  }
}

class ConnectionService(
    val id: UUID = UUID.randomUUID,
    val supervisor: ActorRef,
    val user: User,
    val out: ActorRef,
    val sourceAddress: String) extends ConnectionServiceHelper {

  protected[this] var currentUsername = user.username
  protected[this] var userPreferences = user.preferences

  protected[this] var pendingDebugChannel: Option[ActorRef] = None

  def initialState() = InitialState(user.id, currentUsername, userPreferences)

  override def preStart() = {
    supervisor ! ConnectionStarted(user, id, self)
    out ! initialState()
  }

  override def receiveRequest = {
    // Incoming basic messages
    case mr: MalformedRequest => timeReceive(mr) { log.error(s"MalformedRequest:  [${mr.reason}]: [${mr.content}].") }
    case p: Ping => timeReceive(p) { out ! Pong(p.timestamp) }
    case GetVersion => timeReceive(GetVersion) { out ! VersionResponse(Config.version) }

    case dr: DebugInfo => timeReceive(dr) { handleDebugInfo(dr.data) }

    // Incoming game messages
    case im: InternalMessage => handleInternalMessage(im)

    // Outgoing messages
    case rm: ResponseMessage => handleResponseMessage(rm)

    case x => throw new IllegalArgumentException(s"Unhandled message [${x.getClass.getSimpleName}].")
  }

  override def postStop() = {
    supervisor ! ConnectionStopped(id)
  }

  private[this] def handleInternalMessage(im: InternalMessage) = im match {
    case ct: SendConnectionTrace => timeReceive(ct) { handleConnectionTrace() }
    case ct: SendClientTrace => timeReceive(ct) { handleClientTrace() }
    case x => throw new IllegalArgumentException(s"Unhandled internal message [${x.getClass.getSimpleName}].")
  }
}
