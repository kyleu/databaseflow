package services.query

import java.util.UUID

import akka.actor.ActorRef
import models.queries.InsertRowStatement
import models.user.User
import models.{InsertRowResponse, ResponseMessage}
import services.database.{DatabaseRegistry, DatabaseWorkerPool}
import services.schema.SchemaService
import utils.Logging

object InsertRowService extends Logging {
  def insert(connectionId: UUID, user: User, name: String, params: Map[String, String], resultId: UUID, out: ActorRef) = {
    val t = SchemaService.getTable(connectionId, name).getOrElse(throw new IllegalStateException(s"Missing definition for table [$name]."))
    val db = DatabaseRegistry.db(user, connectionId)
    val statement = InsertRowStatement(name, params, t.columns, db.engine)

    def work() = {
      db.executeUpdate(statement)
      InsertRowResponse(resultId)
    }
    def onSuccess(rm: ResponseMessage) = out ! rm
    def onFailure(t: Throwable) = out ! InsertRowResponse(resultId, Map("general" -> t.getMessage))
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }
}
