package services.query

import java.util.UUID

import akka.actor.ActorRef
import models.queries.dynamic.{InsertRowStatement, UpdateRowStatement}
import models.user.User
import models.{ResponseMessage, RowUpdateResponse}
import services.database.{DatabaseRegistry, DatabaseWorkerPool}
import services.schema.SchemaService
import util.Logging

object RowUpdateService extends Logging {
  def process(connectionId: UUID, user: User, name: String, pk: Seq[(String, String)], params: Map[String, String], resultId: UUID, out: ActorRef) = {
    if (pk.isEmpty) {
      insert(connectionId, user, name, params, resultId, out)
    } else {
      update(connectionId, user, name, pk, params, resultId, out)
    }
  }

  def insert(connectionId: UUID, user: User, name: String, params: Map[String, String], resultId: UUID, out: ActorRef) = {
    def work() = if (params.isEmpty) {
      RowUpdateResponse(resultId, Nil, 0, Map("general" -> "Please select at least one column to insert."))
    } else {
      val t = SchemaService.getTable(connectionId, name).getOrElse(throw new IllegalStateException(s"Missing definition for table [$name]."))
      val db = DatabaseRegistry.db(user, connectionId)
      val statement = InsertRowStatement(name, params, t.columns, db.engine)
      val rowsAffected = db.executeUpdate(statement)
      RowUpdateResponse(resultId, Nil, rowsAffected)
    }
    def onSuccess(rm: ResponseMessage) = out ! rm
    def onFailure(t: Throwable) = out ! RowUpdateResponse(resultId, Nil, 0, Map("general" -> t.getMessage))
    DatabaseWorkerPool.submitWork(work _, onSuccess, onFailure)
  }

  def update(connectionId: UUID, user: User, name: String, pk: Seq[(String, String)], params: Map[String, String], resultId: UUID, out: ActorRef) = {
    def work() = if (params.isEmpty) {
      RowUpdateResponse(resultId, Nil, 0, Map("general" -> "Please select at least one column to update."))
    } else {
      val t = SchemaService.getTable(connectionId, name).getOrElse(throw new IllegalStateException(s"Missing definition for table [$name]."))
      val db = DatabaseRegistry.db(user, connectionId)
      val statement = UpdateRowStatement(name, pk, params, t.columns, db.engine)
      val rowsAffected = db.executeUpdate(statement)
      RowUpdateResponse(resultId, pk, rowsAffected)
    }
    def onSuccess(rm: ResponseMessage) = out ! rm
    def onFailure(t: Throwable) = out ! RowUpdateResponse(resultId, pk, 0, Map("general" -> t.getMessage))
    DatabaseWorkerPool.submitWork(work _, onSuccess, onFailure)
  }
}
