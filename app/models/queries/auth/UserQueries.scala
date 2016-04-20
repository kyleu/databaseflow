package models.queries.auth

import java.util.UUID

import models.database.{ FlatSingleRowQuery, Row, SingleRowQuery, Statement }
import models.queries.BaseQueries
import models.user.{ Role, User, UserPreferences }
import upickle.default._
import utils.JdbcUtils

object UserQueries extends BaseQueries[User] {
  override protected val tableName = "flowusers"
  override protected val columns = Seq("id", "username", "prefs", "profiles", "roles", "created")
  override protected val searchColumns = Seq("id::text", "username")

  val insert = Insert
  val getById = GetById
  def searchCount(q: String, groupBy: Option[String] = None) = new SearchCount(q, groupBy)
  val search = Search
  val removeById = RemoveById

  case class IsUsernameInUse(name: String) extends SingleRowQuery[Boolean] {
    override val sql = "select count(*) as c from users where username = ?"
    override val values = Seq(name)
    override def map(row: Row) = row.as[Long]("c") != 0L
  }

  case class UpdateUser(u: User) extends Statement {
    override val sql = updateSql(Seq("username", "prefs", "profiles", "roles"))
    override val values = {
      // TODO val profiles = u.profiles.map(l => s"${l.providerID}:${l.providerKey}").mkString(",")
      val profiles = u.profiles.mkString(",")
      val roles = u.roles.map(_.toString).mkString(",")
      val prefs = write(u.preferences)
      Seq(u.username, prefs, profiles, roles, u.id)
    }
  }

  case class SetPreferences(userId: UUID, userPreferences: UserPreferences) extends Statement {
    override val sql = updateSql(Seq("prefs"))
    override val values = Seq(write(userPreferences), userId)
  }

  case class AddRole(id: UUID, role: Role) extends Statement {
    override val sql = s"update $tableName set roles = array_append(roles, ?) where id = ?"
    override val values = Seq(role.toString, id)
  }

  case class FindUserByUsername(username: String) extends FlatSingleRowQuery[User] {
    override val sql = getSql(Some("username = ?"))
    override val values = Seq(username)
    override def flatMap(row: Row) = Some(fromRow(row))
  }

  case object CountAdmins extends SingleRowQuery[Int]() {
    override def sql = "select count(*) as c from users where 'admin' = any(roles)"
    override def map(row: Row) = row.as[Long]("c").toInt
  }

  override protected def fromRow(row: Row) = {
    val id = row.as[UUID]("id")
    val profiles = row.as[String]("profiles").split(",").flatMap { l =>
      val info = l.trim
      if (info.nonEmpty) {
        val delimiter = info.indexOf(':')
        val provider = info.substring(0, delimiter)
        val key = info.substring(delimiter + 1)
        // TODO Some(LoginInfo(provider, key))
        Some(info)
      } else {
        None
      }
    }
    val username = row.asOpt[String]("username")
    val prefsString = row.as[String]("prefs")
    val preferences = read[UserPreferences](prefsString)
    val roles = row.as[String]("roles").split(",").map(x => Role(x.trim)).toSet
    val created = JdbcUtils.toLocalDateTime(row, "created")
    User(id, username, preferences, profiles, roles, created)
  }

  override protected def toDataSeq(u: User) = {
    val prefs = write(u.preferences)
    // TODO val profiles = u.profiles.map(l => s"${l.providerID}:${l.providerKey}").mkString(",")
    val profiles = u.profiles.mkString(",")
    val roles = u.roles.map(_.toString).mkString(",")
    Seq(u.id, u.username, prefs, profiles, roles, u.created)
  }
}
