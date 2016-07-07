package services.user

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.queries.auth._
import models.user.{Role, User}
import services.database.MasterDatabaseConnection
import utils.Logging
import utils.cache.UserCache

@javax.inject.Singleton
class UserService @javax.inject.Inject() (hasher: PasswordHasher) extends Logging {
  def getById(id: UUID) = MasterDatabaseConnection.query(UserQueries.getById(Seq(id)))

  def save(user: User, update: Boolean = false): User = {
    log.info(s"${if (update) { "Updating" } else { "Creating" }} user [$user].")
    val statement = if (update) {
      UserQueries.UpdateUser(user)
    } else {
      UserQueries.insert(user)
    }
    MasterDatabaseConnection.executeUpdate(statement)
    UserCache.cacheUser(user)
  }

  def isUsernameInUse(name: String) = MasterDatabaseConnection.query(UserQueries.IsUsernameInUse(name))

  def remove(userId: UUID) = {
    val startTime = System.nanoTime
    MasterDatabaseConnection.transaction { conn =>
      getById(userId).map { user =>
        MasterDatabaseConnection.executeUpdate(PasswordInfoQueries.removeById(Seq(user.profile.providerID, user.profile.providerKey)))
      }
      val users = MasterDatabaseConnection.executeUpdate(UserQueries.removeById(Seq(userId)))
      UserCache.removeUser(userId)
      val timing = ((System.nanoTime - startTime) / 1000000).toInt
      Map("users" -> users, "timing" -> timing)
    }
  }

  def enableAdmin(user: User) = {
    val adminCount = MasterDatabaseConnection.query(UserQueries.CountAdmins)
    if (adminCount == 0) {
      MasterDatabaseConnection.executeUpdate(UserQueries.SetRole(user.id, Role.Admin))
      UserCache.removeUser(user.id)
    } else {
      throw new IllegalStateException("An admin already exists.")
    }
  }

  def getAll = MasterDatabaseConnection.query(UserQueries.getAll("\"username\""))

  def update(id: UUID, username: String, email: String, password: Option[String], role: Role, originalEmail: String) = {
    MasterDatabaseConnection.executeUpdate(UserQueries.UpdateFields(id, username, email, role))
    if (email != originalEmail) {
      MasterDatabaseConnection.executeUpdate(PasswordInfoQueries.UpdateEmail(originalEmail, email))
    }
    password.map { p =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, email)
      val authInfo = hasher.hash(p)
      MasterDatabaseConnection.executeUpdate(PasswordInfoQueries.UpdatePasswordInfo(loginInfo, authInfo))
    }
  }
}
