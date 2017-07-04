package services.user

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.queries.auth._
import models.user.{Role, User}
import services.database.core.MasterDatabase
import utils.Logging
import utils.cache.UserCache

object UserService {
  var instance: Option[UserService] = None
}

@javax.inject.Singleton
class UserService @javax.inject.Inject() (hasher: PasswordHasher) extends Logging {
  UserService.instance match {
    case Some(_) => throw new IllegalStateException("User Service already initialized.")
    case None => UserService.instance = Some(this)
  }

  def getById(id: UUID) = MasterDatabase.query(UserQueries.getById(Seq(id)))

  def save(user: User, update: Boolean = false): User = {
    log.info(s"${if (update) { "Updating" } else { "Creating" }} user [$user].")
    val statement = if (update) {
      UserQueries.UpdateUser(user)
    } else {
      UserQueries.insert(user)
    }
    MasterDatabase.executeUpdate(statement)
    UserCache.cacheUser(user)
  }

  def userCount = MasterDatabase.query(UserQueries.count)

  def isUsernameInUse(name: String) = MasterDatabase.query(UserQueries.IsUsernameInUse(name))

  def usernameLookup(id: UUID) = MasterDatabase.query(UserQueries.GetUsername(id))

  def usernameLookupMulti(ids: Set[UUID]) = if (ids.isEmpty) { Map.empty[UUID, String] } else { MasterDatabase.query(UserQueries.GetUsernames(ids)) }

  def remove(userId: UUID) = {
    val startTime = System.nanoTime
    MasterDatabase.transaction { conn =>
      conn.query(UserQueries.getById(Seq(userId))).map { user =>
        conn.executeUpdate(PasswordInfoQueries.removeById(Seq(user.profile.providerID, user.profile.providerKey)))
      }
      val users = conn.executeUpdate(UserQueries.removeById(Seq(userId)))
      UserCache.removeUser(userId)
      val timing = ((System.nanoTime - startTime) / 1000000).toInt
      Map("users" -> users, "timing" -> timing)
    }
  }

  def getAll = MasterDatabase.query(UserQueries.getAll("\"username\""))

  def update(id: UUID, username: String, email: String, password: Option[String], role: Role, originalEmail: String) = {
    MasterDatabase.executeUpdate(UserQueries.UpdateFields(id, username, email, role))
    if (email != originalEmail) {
      MasterDatabase.executeUpdate(PasswordInfoQueries.UpdateEmail(originalEmail, email))
    }
    password.map { p =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, email)
      val authInfo = hasher.hash(p)
      MasterDatabase.executeUpdate(PasswordInfoQueries.UpdatePasswordInfo(loginInfo, authInfo))
    }
    UserCache.removeUser(id)
  }
}
