package services.user

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import models.queries.auth.PasswordInfoQueries
import services.database.MasterDatabase

import scala.concurrent.Future

object PasswordInfoService extends DelegableAuthInfoDAO[PasswordInfo] {
  override def find(loginInfo: LoginInfo) = {
    Future.successful(MasterDatabase.conn.query(PasswordInfoQueries.getById(Seq(loginInfo.providerID, loginInfo.providerKey))))
  }

  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo) = {
    MasterDatabase.conn.transaction { conn =>
      val rowsAffected = MasterDatabase.conn.executeUpdate(PasswordInfoQueries.UpdatePasswordInfo(loginInfo, authInfo))
      if (rowsAffected == 0) {
        MasterDatabase.conn.executeUpdate(PasswordInfoQueries.CreatePasswordInfo(loginInfo, authInfo))
        Future.successful(authInfo)
      } else {
        Future.successful(authInfo)
      }
    }
  }

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo) = {
    MasterDatabase.conn.executeUpdate(PasswordInfoQueries.CreatePasswordInfo(loginInfo, authInfo))
    Future.successful(authInfo)
  }

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    MasterDatabase.conn.executeUpdate(PasswordInfoQueries.UpdatePasswordInfo(loginInfo, authInfo))
    Future.successful(authInfo)
  }

  override def remove(loginInfo: LoginInfo) = {
    MasterDatabase.conn.executeUpdate(PasswordInfoQueries.removeById(Seq(loginInfo.providerID, loginInfo.providerKey)))
    Future.successful(Unit)
  }
}
