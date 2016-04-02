package services.query

import models.queries.query.SavedQueryQueries
import models.query.SavedQuery
import services.database.MasterDatabase

object SavedQueryService {
  def save(sq: SavedQuery) = MasterDatabase.conn.query(SavedQueryQueries.getById(sq.id)) match {
    case Some(existing) => MasterDatabase.conn.execute(SavedQueryQueries.UpdateSavedQuery(sq))
    case None => MasterDatabase.conn.execute(SavedQueryQueries.insert(sq))
  }
}
