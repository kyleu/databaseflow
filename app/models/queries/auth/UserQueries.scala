package models.queries.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.database.{FlatSingleRowQuery, Row, SingleRowQuery, Statement}
import models.queries.BaseQueries
import models.user.{Role, User, UserPreferences}
import upickle.default._
import utils.JdbcUtils

object UserQueries extends BaseQueries[User] {
  override protected val tableName = "dbf_users"
  override protected val columns = Seq("id", "username", "prefs", "email", "roles", "created")
  override protected val searchColumns = Seq("id::text", "username")

  val insert = Insert
  val getById = GetById
  def getAll(orderBy: String) = GetAll(orderBy = orderBy)
  def searchCount(q: String, groupBy: Option[String] = None) = new SearchCount(q, groupBy)
  val search = Search
  val removeById = RemoveById

  case class IsUsernameInUse(name: String) extends SingleRowQuery[Boolean] {
    override val sql = """select count(*) as c from \"users\" where \"username\" = ?"""
    override val values = Seq(name)
    override def map(row: Row) = row.as[Long]("c") != 0L
  }

  case class UpdateUser(u: User) extends Statement {
    override val sql = updateSql(Seq("username", "prefs", "email", "roles"))
    override val values = {
      val roles = u.roles.mkString(",")
      val prefs = write(u.preferences)
      Seq(u.username, prefs, u.profile.providerKey, roles, u.id)
    }
  }

  case class SetPreferences(userId: UUID, userPreferences: UserPreferences) extends Statement {
    override val sql = updateSql(Seq("prefs"))
    override val values = Seq(write(userPreferences), userId)
  }

  case class SetRoles(id: UUID, roles: Set[Role]) extends Statement {
    override val sql = s"""update \"$tableName\" set \"roles\" = ? where \"id\" = ?"""
    override val values = Seq(roles.mkString(","), id)
  }

  case class FindUserByUsername(username: String) extends FlatSingleRowQuery[User] {
    override val sql = getSql(Some("\"username\" = ?"))
    override val values = Seq(username)
    override def flatMap(row: Row) = Some(fromRow(row))
  }

  case class FindUserByProfile(loginInfo: LoginInfo) extends FlatSingleRowQuery[User] {
    override val sql = getSql(Some("\"email\" = ?"))
    override val values = Seq(loginInfo.providerKey)
    override def flatMap(row: Row) = Some(fromRow(row))
  }

  case object CountAdmins extends SingleRowQuery[Int]() {
    override def sql = "select count(*) as c from \"users\" where \"roles\" like '%admin%'"
    override def map(row: Row) = row.as[Long]("c").toInt
  }

  override protected def fromRow(row: Row) = {
    val id = row.as[UUID]("id")
    val username = row.asOpt[String]("username")
    val prefsString = row.as[String]("prefs")
    val preferences = read[UserPreferences](prefsString)
    val profile = LoginInfo("credentials", row.as[String]("email"))
    val roles = row.as[String]("roles").split(",").map(x => Role(x.trim)).toSet
    val created = JdbcUtils.toLocalDateTime(row, "created")
    User(id, username, preferences, profile, roles, created)
  }

  override protected def toDataSeq(u: User) = {
    val roles = u.roles.map(_.toString).mkString(",")
    val prefs = write(u.preferences)
    Seq(u.id, u.username, prefs, u.profile.providerKey, roles, u.created)
  }
}
