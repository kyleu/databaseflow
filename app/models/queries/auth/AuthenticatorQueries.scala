package models.queries.auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.queries.BaseQueries
import models.database.{ Row, Statement, FlatSingleRowQuery }
import utils.{ JdbcUtils, DateUtils }

object AuthenticatorQueries extends BaseQueries[CookieAuthenticator] {
  override protected val tableName = "session_info"
  override protected val columns = Seq("id", "provider", "key", "last_used", "expiration", "fingerprint", "created")
  override protected val searchColumns = Seq("id::text", "key")

  val insert = Insert
  val getById = GetById
  val removeById = RemoveById

  case class FindSessionInfoByLoginInfo(l: LoginInfo) extends FlatSingleRowQuery[CookieAuthenticator] {
    override val sql = getSql(Some("provider = ? and key = ?"))
    override val values = Seq(l.providerID, l.providerKey)
    override def flatMap(row: Row) = Some(fromRow(row))
  }

  case class UpdateAuthenticator(ca: CookieAuthenticator) extends Statement {
    override val sql = updateSql(Seq("provider", "key", "last_used", "expiration", "fingerprint"))
    override val values = Seq(
      ca.loginInfo.providerID,
      ca.loginInfo.providerKey,
      ca.lastUsedDateTime.toLocalDateTime,
      ca.expirationDateTime.toLocalDateTime,
      ca.fingerprint,
      ca.id
    )
  }

  override protected def fromRow(row: Row) = CookieAuthenticator(
    id = row.as[String]("id"),
    loginInfo = LoginInfo(row.as[String]("provider"), row.as[String]("key")),
    lastUsedDateTime = JdbcUtils.toLocalDateTime(row, "last_used").toDateTime,
    expirationDateTime = JdbcUtils.toLocalDateTime(row, "expiration").toDateTime,
    idleTimeout = None,
    cookieMaxAge = None,
    fingerprint = row.asOpt[String]("fingerprint")
  )

  override protected def toDataSeq(ca: CookieAuthenticator) = Seq(
    ca.id,
    ca.loginInfo.providerID,
    ca.loginInfo.providerKey,
    ca.lastUsedDateTime.toLocalDateTime,
    ca.expirationDateTime.toLocalDateTime,
    ca.fingerprint,
    DateUtils.now
  )
}
