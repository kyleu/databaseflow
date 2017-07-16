package services.socket

import akka.actor.PoisonPill
import models._
import models.schema.Schema
import services.database.{DatabaseRegistry, DatabaseWorkerPool}
import services.query.{SavedQueryService, SharedResultService}
import services.schema.{SchemaRefreshService, SchemaService}
import services.supervisor.ActorSupervisor
import util.{ExceptionUtils, Logging}

import scala.util.{Failure, Success, Try}

trait StartHelper extends Logging { this: SocketService =>
  protected[this] def attemptConnect() = DatabaseRegistry.databaseForUser(user, connectionId) match {
    case Right(d) => d
    case Left(x) =>
      log.warn(messages("socket.connect.error", Nil), x)
      out ! ServerError(messages("socket.connect.failed", Nil), x.getMessage)
      out ! PoisonPill
      self ! PoisonPill
      throw x
  }

  protected[this] def onStart() = {
    log.info(s"Starting connection for user [${user.id}: ${user.username}].")

    supervisor ! SocketStarted(user, id, self)
    out ! UserSettings(user.id, user.username, user.profile.providerKey, user.preferences)

    SavedQueryService.getForUser(user, connectionId, Some(out))
    SharedResultService.getForUser(user, connectionId, Some(out))
    refreshSchema(false)
  }

  protected[this] def refreshSchema(forceRefresh: Boolean) = {
    def onSchemaSuccess(t: Try[Schema]): Unit = t match {
      case Success(sch) =>
        schema = Some(sch)
        out ! SchemaResponse(sch)
        if (sch.detailsLoadedAt.isEmpty) {
          def onRefreshSuccess(s: Schema) = {
            schema = Some(s)
            out ! SchemaResponse(s)
          }
          def onRefreshFailure(x: Throwable) = ExceptionUtils.actorErrorFunction(out, "SchemaDetailError", x)
          SchemaRefreshService.refreshSchema(db, onRefreshSuccess, onRefreshFailure)
        }

      case Failure(x) => ExceptionUtils.actorErrorFunction(out, "SchemaLoadError", x)
    }
    def onSchemaFailure(t: Throwable) = { ExceptionUtils.actorErrorFunction(out, "SchemaLoadException", t) }
    DatabaseWorkerPool.submitWork(() => SchemaService.getSchema(db, forceRefresh = forceRefresh), onSchemaSuccess, onSchemaFailure)
  }
}
