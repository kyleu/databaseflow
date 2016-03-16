package services.connection

import java.util.UUID

import akka.actor.{ Props, ActorRef }
import models._
import models.database.{ Row, Query }
import models.engine.ConnectionSettings
import models.user.User
import services.database.MasterDatabase
import utils.{ Logging, Config }
import utils.metrics.InstrumentedActor

object ConnectionService {
  def props(id: Option[UUID], supervisor: ActorRef, connectionId: UUID, user: User, out: ActorRef, sourceAddress: String) = {
    Props(new ConnectionService(id.getOrElse(UUID.randomUUID), supervisor, connectionId, user, out, sourceAddress))
  }
}

class ConnectionService(
  val id: UUID = UUID.randomUUID,
  val supervisor: ActorRef,
  val connectionId: UUID,
  val user: User,
  val out: ActorRef,
  val sourceAddress: String
) extends InstrumentedActor with ConnectionServiceTraceHelper with Logging {

  protected[this] var currentUsername = user.username
  protected[this] var userPreferences = user.preferences
  protected[this] val db = MasterDatabase.databaseFor(connectionId)

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
    case sq: SubmitQuery => timeReceive(sq) { handleSubmitQuery(sq.sql, sq.action.getOrElse("run")) }
    case im: InternalMessage => handleInternalMessage(im)
    case rm: ResponseMessage => out ! rm
    case x => throw new IllegalArgumentException(s"Unhandled message [${x.getClass.getSimpleName}].")
  }

  override def postStop() = {
    supervisor ! ConnectionStopped(id)
  }

  private[this] def handleSubmitQuery(sql: String, action: String) = {
    log.info(s"Performing query action [$action] for sql [$sql].")

    val fullSql = action match {
      case "run" => sql
      case "explain" => "explain " + sql
      case "analyze" => "explain analyze " + sql
    }

    val q = new Query[(Seq[String], Seq[Seq[Any]])] {
      override def sql = fullSql
      override def reduce(rows: Iterator[Row]) = {
        val columns = Seq("a", "b", "c")
        val data = Seq(
          Seq(1, 2, 3),
          Seq(4, 5, 6),
          Seq(7, 8, 9)
        )
        columns -> data
      }
    }

    val result = db.query(q)

    log.info(s"Query result: [$result].")
  }

  private[this] def handleInternalMessage(im: InternalMessage) = im match {
    case ct: SendConnectionTrace => timeReceive(ct) { handleConnectionTrace() }
    case ct: SendClientTrace => timeReceive(ct) { handleClientTrace() }
    case x => throw new IllegalArgumentException(s"Unhandled internal message [${x.getClass.getSimpleName}].")
  }
}
