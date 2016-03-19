package services.connection

import java.sql.SQLSyntaxErrorException
import java.util.UUID

import akka.actor.{ ActorRef, Props }
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException
import models._
import models.queries.DynamicQuery
import models.query.{ QueryError, QueryResult }
import models.template.QueryPlanTemplate
import models.user.User
import org.postgresql.util.PSQLException
import services.database.MasterDatabase
import services.schema.SchemaService
import utils.metrics.InstrumentedActor
import utils.{ DateUtils, Config, Logging }

import scala.util.control.NonFatal

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
  protected[this] val schema = SchemaService.getSchema(db.source)

  protected[this] var pendingDebugChannel: Option[ActorRef] = None

  override def preStart() = {
    supervisor ! ConnectionStarted(user, id, self)
    out ! InitialState(user.id, currentUsername, userPreferences, schema)
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

  private[this] def handleSubmitQuery(sql: String, action: String) = action match {
    case "run" => handleRunQuery(sql)
    case "explain" => handleExplainQuery(sql)
    case _ => throw new IllegalArgumentException(action)
  }

  private[this] def handleRunQuery(sql: String) = {
    log.info(s"Performing query action [run] for sql [$sql].")
    val id = UUID.randomUUID
    val startMs = DateUtils.nowMillis
    try {
      val result = db.query(DynamicQuery(sql))
      //log.info(s"Query result: [$result].")
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryResultResponse(id, QueryResult(sql, result._1, result._2), durationMs)
    } catch {
      case sqlEx: PSQLException =>
        val e = sqlEx.getServerErrorMessage
        val durationMs = (DateUtils.nowMillis - startMs).toInt
        out ! QueryErrorResponse(id, QueryError(sql, e.getSQLState, e.getMessage, Some(e.getLine), Some(e.getPosition)), durationMs)
      case sqlEx: SQLSyntaxErrorException =>
        val durationMs = (DateUtils.nowMillis - startMs).toInt
        out ! QueryErrorResponse(id, QueryError(sql, sqlEx.getSQLState, sqlEx.getMessage), durationMs)
      case NonFatal(x) =>
        log.warn(s"Error running sql [$sql].", x)
        val error = ServerError(x.getClass.getSimpleName, x.getMessage)
        out ! error
    }
  }

  private[this] def handleExplainQuery(sql: String) = {
    out ! QueryPlanTemplate.testPlan
  }

  private[this] def handleInternalMessage(im: InternalMessage) = im match {
    case ct: SendConnectionTrace => timeReceive(ct) { handleConnectionTrace() }
    case ct: SendClientTrace => timeReceive(ct) { handleClientTrace() }
    case x => throw new IllegalArgumentException(s"Unhandled internal message [${x.getClass.getSimpleName}].")
  }
}
