package services.socket

import akka.actor.PoisonPill
import models._
import models.schema.Schema
import services.database.{DatabaseRegistry, DatabaseWorkerPool}
import services.query.SavedQueryService
import services.schema.SchemaService
import utils.{ExceptionUtils, Logging}

import scala.util.{Failure, Success, Try}

trait StartHelper extends Logging { this: SocketService =>
  protected[this] def attemptConnect() = DatabaseRegistry.databaseFor(user, connectionId) match {
    case Right(d) => Some(d)
    case Left(x) =>
      log.warn("Error attempting to connect to database.", x)
      out ! ServerError("Database Connect Failed", x.getMessage)
      out ! PoisonPill
      self ! PoisonPill
      None
  }

  protected[this] def onStart() = {
    log.info(s"Starting connection for user [${user.map(_.id).getOrElse("")}: ${currentUsername.getOrElse("")}].")

    supervisor ! SocketStarted(user, id, self)
    out ! UserSettings(user.map(_.id), currentUsername, user.map(_.profile.providerKey), userPreferences)

    val sqq = SavedQueryService.getForUser(user.map(u => u.id -> u.role), connectionId, out)

    refreshSchema(false)
  }

  protected[this] def refreshSchema(forceRefresh: Boolean) = {
    def onSchemaSuccess(t: Try[Schema]): Unit = t match {
      case Success(sch) =>
        schema = Some(sch)
        out ! SchemaResultResponse(sch)
        if (sch.detailsLoadedAt.isEmpty) {
          def onRefreshSuccess(s: Schema) = {
            schema = Some(s)
            out ! SchemaResultResponse(s)
          }
          def onRefreshFailure(x: Throwable) = ExceptionUtils.actorErrorFunction(out, "SchemaDetailError", x)
          SchemaService.refreshSchema(db, onRefreshSuccess, onRefreshFailure)
        }

      case Failure(x) => ExceptionUtils.actorErrorFunction(out, "SchemaLoadError", x)
    }
    def onSchemaFailure(t: Throwable) { ExceptionUtils.actorErrorFunction(out, "SchemaLoadException", t) }
    DatabaseWorkerPool.submitWork(() => SchemaService.getSchema(db, forceRefresh = forceRefresh), onSchemaSuccess, onSchemaFailure)
  }
}
