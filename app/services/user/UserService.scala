package services.user

import java.util.UUID

import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.queries.auth._
import models.user.{ Role, User }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.database.MasterDatabase
import utils.Logging
import utils.cache.UserCache

import scala.concurrent.Future

object UserService extends Logging {
  def create(currentUser: User, profile: CommonSocialProfile): Future[User] = {
    log.info(s"Saving profile [$profile].")
    UserSearchService.retrieve(profile.loginInfo).map {
      case Some(existingUser) => if (existingUser.id == currentUser.id) {
        val profiles = existingUser.profiles.filterNot(_.providerID == profile.loginInfo.providerID) :+ profile.loginInfo
        val u = existingUser.copy(profiles = profiles)
        save(u, update = true)
      } else {
        existingUser
      }
      case None =>
        MasterDatabase.db.execute(ProfileQueries.insert(profile))
        val u = currentUser.copy(
          profiles = currentUser.profiles.filterNot(_.providerID == profile.loginInfo.providerID) :+ profile.loginInfo
        )
        save(u, update = true)
    }
  }

  def save(user: User, update: Boolean = false): User = {
    log.info(s"${if (update) { "Updating" } else { "Creating" }} user [$user].")
    val statement = if (update) {
      UserQueries.UpdateUser(user)
    } else {
      UserQueries.insert(user)
    }
    MasterDatabase.db.execute(statement)
    UserCache.cacheUser(user)
  }

  def isUsernameInUse(name: String) = MasterDatabase.db.query(UserQueries.IsUsernameInUse(name))

  def remove(userId: UUID) = {
    val startTime = System.nanoTime
    MasterDatabase.db.transaction { conn =>
      val profiles = removeProfiles(userId).length
      val users = MasterDatabase.db.execute(UserQueries.removeById(Seq(userId)))
      UserCache.removeUser(userId)
      val timing = ((System.nanoTime - startTime) / 1000000).toInt
      Map("users" -> users, "profiles" -> profiles, "timing" -> timing)
    }
  }

  def enableAdmin(user: User) = {
    val adminCount = MasterDatabase.db.query(UserQueries.CountAdmins)
    if (adminCount == 0) {
      MasterDatabase.db.execute(UserQueries.AddRole(user.id, Role.Admin))
      UserCache.removeUser(user.id)
    } else {
      throw new IllegalStateException("An admin already exists.")
    }
  }

  private[this] def removeProfiles(userId: UUID) = {
    val profiles = MasterDatabase.db.query(ProfileQueries.FindProfilesByUser(userId))
    profiles.map { profile =>
      profile.loginInfo.providerID match {
        case "credentials" => MasterDatabase.db.execute(PasswordInfoQueries.removeById(Seq(profile.loginInfo.providerID, profile.loginInfo.providerKey)))
        case p => throw new IllegalArgumentException(s"Unknown provider [$p].")
      }
      MasterDatabase.db.execute(ProfileQueries.remove(Seq(profile.loginInfo.providerID, profile.loginInfo.providerKey)))
    }
    profiles
  }
}
