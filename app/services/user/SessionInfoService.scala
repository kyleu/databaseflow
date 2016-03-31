package services.user

import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.daos.AuthenticatorDAO
import models.queries.auth.AuthenticatorQueries
import services.database.MasterDatabase
import utils.cache.UserCache

import scala.concurrent.Future

object SessionInfoService extends AuthenticatorDAO[CookieAuthenticator] {
  override def find(id: String) = UserCache.getSession(id) match {
    case Some(sess) => Future.successful(Some(sess))
    case None => Future.successful(MasterDatabase.conn.query(AuthenticatorQueries.getById(Seq(id))).map(UserCache.cacheSession))
  }

  override def add(session: CookieAuthenticator) = {
    MasterDatabase.conn.execute(AuthenticatorQueries.insert(session))
    UserCache.cacheSession(session)
    Future.successful(session)
  }

  override def update(session: CookieAuthenticator) = {
    MasterDatabase.conn.execute(AuthenticatorQueries.UpdateAuthenticator(session))
    UserCache.cacheSession(session)
    Future.successful(session)
  }

  override def remove(id: String) = {
    MasterDatabase.conn.execute(AuthenticatorQueries.removeById(Seq(id)))
    UserCache.removeSession(id)
    Future.successful(Unit)
  }
}
