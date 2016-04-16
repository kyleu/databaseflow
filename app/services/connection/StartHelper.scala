package services.connection

import models._
import services.database.MasterDatabase
import services.schema.SchemaService
import utils.Logging

import scala.util.{ Failure, Success }

trait StartHelper extends Logging { this: ConnectionService =>
  protected[this] def attemptConnect() = MasterDatabase.databaseFor(connectionId) match {
    case Right(d) => Some(d)
    case Left(x) =>
      log.warn("Error attempting to connect to database.", x)
      out ! ServerError("Database Connect Failed", x.getMessage)
      None
  }

  protected[this] def onStart() = {
    supervisor ! ConnectionStarted(user, id, self)
    out ! SavedQueryResultResponse(savedQueries, 0)
    schema.foreach { s =>
      val is = InitialState(user.map(_.id), currentUsername, userPreferences, s)
      out ! is
    }
    if (schema.forall(_.detailsLoadedAt.isEmpty)) {
      schema = SchemaService.refreshSchema(db) match {
        case Success(s) => Some(s)
        case Failure(x) =>
          log.error("Unable to refresh schema.", x)
          out ! ServerError("SchemaDetailError", s"${x.getClass.getSimpleName} - ${x.getMessage}")
          None
      }
      schema.foreach { s =>
        out ! TableResultResponse(s.tables, 0)
        out ! ViewResultResponse(s.views, 0)
        out ! ProcedureResultResponse(s.procedures, 0)
      }
    }
  }
}
