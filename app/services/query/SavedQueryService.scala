package services.query

import java.util.UUID

import models.query.SavedQuery
import models.user.User
import utils.DateUtils

object SavedQueryService {
  def getSavedQueries(user: User) = Seq(
    SavedQuery(
      id = UUID.randomUUID,
      owner = user.id,
      title = "Saved Query 1",
      sql = "select * from stuff",
      lastRan = None,
      created = DateUtils.nowMillis,
      updated = DateUtils.nowMillis
    ),
    SavedQuery(
      id = UUID.randomUUID,
      owner = user.id,
      title = "Saved Query 2",
      sql = "select * from stuff",
      lastRan = None,
      created = DateUtils.nowMillis,
      updated = DateUtils.nowMillis
    ),
    SavedQuery(
      id = UUID.randomUUID,
      owner = user.id,
      title = "Saved Query 3",
      sql = "select * from stuff",
      lastRan = None,
      created = DateUtils.nowMillis,
      updated = DateUtils.nowMillis
    )
  )
}
