package services.user

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import models.queries.auth.PasswordInfoQueries
import services.database.MasterDatabase

import scala.concurrent.Future

@javax.inject.Singleton
class PasswordInfoService @javax.inject.Inject() () extends DelegableAuthInfoDAO[PasswordInfo] {
  override def find(loginInfo: LoginInfo) = {
    Future.successful(MasterDatabase.query(PasswordInfoQueries.getById(Seq(loginInfo.providerID, loginInfo.providerKey))))
  }

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo) = {
    MasterDatabase.executeUpdate(PasswordInfoQueries.CreatePasswordInfo(loginInfo, authInfo))
    Future.successful(authInfo)
  }

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    MasterDatabase.executeUpdate(PasswordInfoQueries.UpdatePasswordInfo(loginInfo, authInfo))
    Future.successful(authInfo)
  }

  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo) = {
    MasterDatabase.transaction { conn =>
      val rowsAffected = MasterDatabase.executeUpdate(PasswordInfoQueries.UpdatePasswordInfo(loginInfo, authInfo))
      if (rowsAffected == 0) {
        MasterDatabase.executeUpdate(PasswordInfoQueries.CreatePasswordInfo(loginInfo, authInfo))
        Future.successful(authInfo)
      } else {
        Future.successful(authInfo)
      }
    }
  }

  override def remove(loginInfo: LoginInfo) = {
    MasterDatabase.executeUpdate(PasswordInfoQueries.removeById(Seq(loginInfo.providerID, loginInfo.providerKey)))
    Future.successful(Unit)
  }
}
