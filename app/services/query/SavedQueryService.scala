package services.query

import java.util.UUID

import models.queries.query.SavedQueryQueries
import models.query.SavedQuery
import services.database.MasterDatabase

object SavedQueryService {
  def save(sq: SavedQuery, userId: Option[UUID] = None) = {
    MasterDatabase.conn.query(SavedQueryQueries.getById(sq.id)) match {
      case Some(existing) =>
        if (userId.isEmpty || existing.owner.isEmpty || existing.owner.contains(userId.getOrElse(throw new IllegalStateException()))) {
          val updated = sq.copy(
            owner = existing.owner,
            updated = System.currentTimeMillis
          )
          MasterDatabase.conn.executeUpdate(SavedQueryQueries.UpdateSavedQuery(updated))
          updated
        } else {
          throw new IllegalStateException("Not Authorized.")
        }
      case None =>
        val inserted = sq.copy(owner = userId, created = System.currentTimeMillis, updated = System.currentTimeMillis)
        MasterDatabase.conn.executeUpdate(SavedQueryQueries.insert(inserted))
        inserted
    }
  }

  def delete(id: UUID, userId: Option[UUID] = None) = {
    MasterDatabase.conn.query(SavedQueryQueries.getById(id)) match {
      case Some(existing) => MasterDatabase.conn.executeUpdate(SavedQueryQueries.removeById(id))
      case None => throw new IllegalStateException(s"Unknown saved query [$id].")
    }
  }
}
