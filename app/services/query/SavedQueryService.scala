package services.query

import java.util.UUID

import models.queries.query.SavedQueryQueries
import models.query.SavedQuery
import services.database.MasterDatabaseConnection

object SavedQueryService {
  def save(sq: SavedQuery, userId: Option[UUID] = None) = {
    MasterDatabaseConnection.query(SavedQueryQueries.getById(sq.id)) match {
      case Some(existing) =>
        if (userId.isEmpty || existing.owner.isEmpty || existing.owner.contains(userId.getOrElse(throw new IllegalStateException()))) {
          val updated = sq.copy(
            owner = existing.owner,
            updated = System.currentTimeMillis
          )
          MasterDatabaseConnection.executeUpdate(SavedQueryQueries.UpdateSavedQuery(updated))
          updated
        } else {
          throw new IllegalStateException("Not Authorized.")
        }
      case None =>
        val inserted = sq.copy(owner = userId, created = System.currentTimeMillis, updated = System.currentTimeMillis)
        MasterDatabaseConnection.executeUpdate(SavedQueryQueries.insert(inserted))
        inserted
    }
  }

  def delete(id: UUID, userId: Option[UUID] = None) = {
    MasterDatabaseConnection.query(SavedQueryQueries.getById(id)) match {
      case Some(existing) => if (existing.owner.isEmpty || existing.owner.forall(x => userId.contains(x))) {
        MasterDatabaseConnection.executeUpdate(SavedQueryQueries.removeById(id))
      } else {
        throw new IllegalStateException(s"Attempted by [$userId] to remove saved query [$id], which is owned by [${existing.owner.getOrElse("None")}].")
      }
      case None => throw new IllegalStateException(s"Unknown saved query [$id].")
    }
  }
}
