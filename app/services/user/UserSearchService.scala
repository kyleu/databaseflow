package services.user

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.queries.auth.UserQueries
import models.user.User
import services.database.MasterDatabase
import utils.Logging
import utils.cache.UserCache

import scala.concurrent.Future

object UserSearchService extends IdentityService[User] with Logging {
  def retrieve(id: UUID): Option[User] = UserCache.getUser(id).orElse {
    MasterDatabase.db.query(UserQueries.getById(Seq(id))).map(UserCache.cacheUser)
  }


  def retrieve(username: String): Option[User] = MasterDatabase.db.query(UserQueries.FindUserByUsername(username))

  override def retrieve(loginInfo: LoginInfo) = UserCache.getUserByLoginInfo(loginInfo) match {
    case Some(u) => Future.successful(Some(u))
    case None => if (loginInfo.providerID == "anonymous") {
      MasterDatabase.db.query(UserQueries.getById(Seq(UUID.fromString(loginInfo.providerKey)))) match {
        case Some(dbUser) => if (dbUser.profiles.nonEmpty) {
          log.warn(s"Attempt to authenticate as anonymous for user with profiles [${dbUser.profiles}].")
          Future.successful(None)
        } else {
          UserCache.cacheUser(dbUser)
          Future.successful(Some(dbUser))
        }
        case None => Future.successful(None)
      }
    } else {
      Future.successful(MasterDatabase.db.query(UserQueries.FindUserByProfile(loginInfo)).map(UserCache.cacheUser))
    }
  }
}
