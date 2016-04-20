package services.user

import java.util.UUID

import models.queries.auth.UserQueries
import models.user.User
import services.database.MasterDatabase
import utils.Logging
import utils.cache.UserCache

object UserSearchService extends Logging {
  def retrieve(id: UUID): Option[User] = UserCache.getUser(id).orElse {
    MasterDatabase.conn.query(UserQueries.getById(Seq(id))).map(UserCache.cacheUser)
  }

  def retrieve(username: String): Option[User] = MasterDatabase.conn.query(UserQueries.FindUserByUsername(username))
}
