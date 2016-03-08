package services.supervisor

import java.util.UUID

import akka.actor.{ ActorRef, SupervisorStrategy, OneForOneStrategy }
import akka.actor.SupervisorStrategy.Stop
import models._
import models.user.User
import org.joda.time.LocalDateTime
import utils.metrics.{ InstrumentedActor, MetricsServletActor }
import utils.{ ApplicationContext, DateUtils, Logging }

object ActorSupervisor {
  case class ConnectionRecord(userId: UUID, name: String, actorRef: ActorRef, started: LocalDateTime)
}

class ActorSupervisor(val ctx: ApplicationContext) extends InstrumentedActor with Logging {
  import ActorSupervisor._

  protected[this] val connections = collection.mutable.HashMap.empty[UUID, ConnectionRecord]
  protected[this] val connectionsCounter = metrics.counter("active-connections")

  override def preStart() {
    context.actorOf(MetricsServletActor.props(ctx.config), "metrics-servlet")
  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case _ => Stop
  }

  override def receiveRequest = {
    case cs: ConnectionStarted => timeReceive(cs) { handleConnectionStarted(cs.user, cs.connectionId, cs.conn) }
    case cs: ConnectionStopped => timeReceive(cs) { handleConnectionStopped(cs.connectionId) }

    case GetSystemStatus => timeReceive(GetSystemStatus) { handleGetSystemStatus() }
    case ct: SendConnectionTrace => timeReceive(ct) { handleSendConnectionTrace(ct) }
    case ct: SendClientTrace => timeReceive(ct) { handleSendClientTrace(ct) }

    case im: InternalMessage => log.warn(s"Unhandled internal message [${im.getClass.getSimpleName}] received.")
    case x => log.warn(s"ActorSupervisor encountered unknown message: ${x.toString}")
  }

  private[this] def handleGetSystemStatus() = {
    val connectionStatuses = connections.toList.sortBy(_._2.name).map(x => x._1 -> x._2.name)
    sender() ! SystemStatus(connectionStatuses)
  }

  private[this] def handleSendConnectionTrace(ct: SendConnectionTrace) = connections.find(_._1 == ct.id) match {
    case Some(c) => c._2.actorRef forward ct
    case None => sender() ! ServerError(s"Unknown Connection [${ct.id}].", ct.id.toString)
  }

  private[this] def handleSendClientTrace(ct: SendClientTrace) = connections.find(_._1 == ct.id) match {
    case Some(c) => c._2.actorRef forward ct
    case None => sender() ! ServerError(s"Unknown Client Connection [${ct.id}].", ct.id.toString)
  }

  protected[this] def handleConnectionStarted(user: User, connectionId: UUID, conn: ActorRef) {
    log.debug(s"Connection [$connectionId] registered to [${user.username.getOrElse(user.id)}] with path [${conn.path}].")
    connections(connectionId) = ConnectionRecord(user.id, user.username.getOrElse("Guest"), conn, DateUtils.now)
    connectionsCounter.inc()
  }

  protected[this] def handleConnectionStopped(id: UUID) {
    connections.remove(id) match {
      case Some(conn) =>
        connectionsCounter.dec()
        log.debug(s"Connection [$id] [${conn.actorRef.path}] stopped.")
      case None => log.warn(s"Connection [$id] stopped but is not registered.")
    }
  }
}
