package services.user

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.queries.auth._
import models.user.{Role, User}
import services.database.MasterDatabase
import utils.Logging
import utils.cache.UserCache

@javax.inject.Singleton
class UserService @javax.inject.Inject() (hasher: PasswordHasher) extends Logging {
  def getById(id: UUID) = MasterDatabase.conn.query(UserQueries.getById(Seq(id)))

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
      getById(userId).map { user =>
        MasterDatabase.conn.executeUpdate(PasswordInfoQueries.removeById(Seq(user.profile.providerID, user.profile.providerKey)))
      }
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

  def update(id: UUID, username: String, email: String, password: Option[String], roles: Set[Role], originalEmail: String) = {
    MasterDatabase.conn.executeUpdate(UserQueries.UpdateFields(id, username, email, roles))
    if (email != originalEmail) {
      MasterDatabase.conn.executeUpdate(PasswordInfoQueries.UpdateEmail(originalEmail, email))
    }
    password.map { p =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, email)
      val authInfo = hasher.hash(p)
      MasterDatabase.conn.executeUpdate(PasswordInfoQueries.UpdatePasswordInfo(loginInfo, authInfo))
    }
  }
}
