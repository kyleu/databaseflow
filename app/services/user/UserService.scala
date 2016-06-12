package services.user

import java.util.UUID

import models.queries.auth._
import models.user.{Role, User}
import services.database.MasterDatabase
import utils.Logging
import utils.cache.UserCache

@javax.inject.Singleton
class UserService @javax.inject.Inject() () extends Logging {
  def save(user: User, update: Boolean = false): User = {
    log.info(s"${if (update) { "Updating" } else { "Creating" }} user [$user].")
    val statement = if (update) {
      UserQueries.UpdateUser(user)
    } else {
      UserQueries.insert(user)
    }
    MasterDatabase.conn.executeUpdate(statement)
    UserCache.cacheUser(user)
  }

  def isUsernameInUse(name: String) = MasterDatabase.conn.query(UserQueries.IsUsernameInUse(name))

  def remove(userId: UUID) = {
    val startTime = System.nanoTime
    MasterDatabase.conn.transaction { conn =>
      val users = MasterDatabase.conn.executeUpdate(UserQueries.removeById(Seq(userId)))
      UserCache.removeUser(userId)
      val timing = ((System.nanoTime - startTime) / 1000000).toInt
      Map("users" -> users, "timing" -> timing)
    }
  }

  def enableAdmin(user: User) = {
    val adminCount = MasterDatabase.conn.query(UserQueries.CountAdmins)
    if (adminCount == 0) {
      MasterDatabase.conn.executeUpdate(UserQueries.SetRoles(user.id, user.roles + Role.Admin))
      UserCache.removeUser(user.id)
    } else {
      throw new IllegalStateException("An admin already exists.")
    }
  }

  def getAll = MasterDatabase.conn.query(UserQueries.getAll("\"username\""))
}
