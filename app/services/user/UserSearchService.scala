package services.user

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.queries.auth.UserQueries
import models.user.User
import services.database.MasterDatabaseConnection
import utils.Logging
import utils.cache.UserCache

import scala.concurrent.Future

@javax.inject.Singleton
class UserSearchService @javax.inject.Inject() () extends IdentityService[User] with Logging {
  def retrieve(id: UUID): Option[User] = UserCache.getUser(id).orElse {
    MasterDatabaseConnection.query(UserQueries.getById(Seq(id))).map(UserCache.cacheUser)
  }

  def retrieve(username: String): Option[User] = MasterDatabaseConnection.query(UserQueries.FindUserByUsername(username))

  override def retrieve(loginInfo: LoginInfo) = UserCache.getUserByLoginInfo(loginInfo) match {
    case Some(u) => Future.successful(Some(u))
    case None => Future.successful(MasterDatabaseConnection.query(UserQueries.FindUserByProfile(loginInfo)).map(UserCache.cacheUser))
  }
}
