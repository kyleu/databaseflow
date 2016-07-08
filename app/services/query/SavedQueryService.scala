package services.query

import java.util.UUID

import akka.actor.ActorRef
import models.SavedQueryResultResponse
import models.queries.query.SavedQueryQueries
import models.query.SavedQuery
import models.user.{Role, User}
import services.database.{DatabaseWorkerPool, MasterDatabase}
import utils.ExceptionUtils

object SavedQueryService {
  def canRead(user: Option[User], sq: SavedQuery) = Role.matchPermissions(user, sq.owner, "query", "read", sq.read)
  def canEdit(user: Option[User], sq: SavedQuery) = Role.matchPermissions(user, sq.owner, "query", "edit", sq.edit)

  def getForUser(user: Option[User], connectionId: UUID, out: ActorRef) = {
    val sqq = SavedQueryQueries.getForUser(user.map(_.id), connectionId)
    def onSavedQueriesSuccess(savedQueries: Seq[SavedQuery]) {
      val viewable = savedQueries.filter(sq => canRead(user, sq)._1)
      out ! SavedQueryResultResponse(viewable, 0)
    }
    def onSavedQueriesFailure(t: Throwable) { ExceptionUtils.actorErrorFunction(out, "SavedQueryLoadException", t) }
    DatabaseWorkerPool.submitQuery(sqq, MasterDatabase.conn, onSavedQueriesSuccess, onSavedQueriesFailure)
  }

  def save(sq: SavedQuery, userId: Option[UUID] = None) = {
    MasterDatabase.query(SavedQueryQueries.getById(sq.id)) match {
      case Some(existing) =>
        if (userId.isEmpty || existing.owner.isEmpty || existing.owner.contains(userId.getOrElse(throw new IllegalStateException()))) {
          val updated = sq.copy(
            owner = existing.owner,
            updated = System.currentTimeMillis
          )
          MasterDatabase.executeUpdate(SavedQueryQueries.UpdateSavedQuery(updated))
          updated
        } else {
          throw new IllegalStateException("Not Authorized.")
        }
      case None =>
        val inserted = sq.copy(owner = userId, created = System.currentTimeMillis, updated = System.currentTimeMillis)
        MasterDatabase.executeUpdate(SavedQueryQueries.insert(inserted))
        inserted
    }
  }

  def delete(id: UUID, userId: Option[UUID] = None) = {
    MasterDatabase.query(SavedQueryQueries.getById(id)) match {
      case Some(existing) => if (existing.owner.isEmpty || existing.owner.forall(x => userId.contains(x))) {
        MasterDatabase.executeUpdate(SavedQueryQueries.removeById(id))
      } else {
        throw new IllegalStateException(s"Attempted by [$userId] to remove saved query [$id], which is owned by [${existing.owner.getOrElse("None")}].")
      }
      case None => throw new IllegalStateException(s"Unknown saved query [$id].")
    }
  }
}
