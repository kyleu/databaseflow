package models.queries.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.database.{FlatSingleRowQuery, Row, SingleRowQuery, Statement}
import models.queries.BaseQueries
import models.user.{Role, User, UserPreferences}
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import utils.JdbcUtils
import utils.json.UserSerializers._

object UserQueries extends BaseQueries[User] {
  override protected val tableName = "flow_users"
  override protected val columns = Seq("id", "username", "prefs", "profiles", "roles", "created")
  override protected val searchColumns = Seq("id::text", "username")

  val insert = Insert
  val getById = GetById
  def searchCount(q: String, groupBy: Option[String] = None) = new SearchCount(q, groupBy)
  val search = Search
  val removeById = RemoveById

  private[this] val defaultPreferencesJson = Json.toJson(UserPreferences()).as[JsObject]

  final case class IsUsernameInUse(name: String) extends SingleRowQuery[Boolean] {
    override val sql = "select count(*) as c from users where username = ?"
    override val values = Seq(name)
    override def map(row: Row) = row.as[Long]("c") != 0L
  }

  final case class UpdateUser(u: User) extends Statement {
    override val sql = updateSql(Seq("username", "prefs", "profiles", "roles"))
    override val values = {
      val profiles = u.profiles.map(l => s"${l.providerID}:${l.providerKey}").toArray
      val roles = "{" + u.roles.map(_.toString).mkString(", ") + "}"
      val prefs = Json.toJson(u.preferences).toString()
      Seq(u.username, prefs, profiles, roles, u.id)
    }
  }

  final case class SetPreferences(userId: UUID, userPreferences: UserPreferences) extends Statement {
    override val sql = updateSql(Seq("prefs"))
    override val values = Seq(Json.toJson(userPreferences).toString(), userId)
  }

  final case class AddRole(id: UUID, role: Role) extends Statement {
    override val sql = s"update $tableName set roles = array_append(roles, ?) where id = ?"
    override val values = Seq(role.toString, id)
  }

  final case class FindUserByUsername(username: String) extends FlatSingleRowQuery[User] {
    override val sql = getSql(Some("username = ?"))
    override val values = Seq(username)
    override def flatMap(row: Row) = Some(fromRow(row))
  }

  final case class FindUserByProfile(loginInfo: LoginInfo) extends FlatSingleRowQuery[User] {
    override val sql = getSql(Some("profiles @> ARRAY[?]::text[]"))
    override val values = Seq(s"${loginInfo.providerID}:${loginInfo.providerKey}")
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
      if(info.nonEmpty) {
        val delimiter = info.indexOf(':')
        val provider = info.substring(0, delimiter)
        val key = info.substring(delimiter + 1)
        Some(LoginInfo(provider, key))
      } else {
        None
      }
    }
    val username = row.asOpt[String]("username")
    val prefsString = row.as[String]("prefs")
    val prefsJson = Json.parse(prefsString).as[JsObject]
    val preferences = Json.fromJson[UserPreferences](defaultPreferencesJson ++ prefsJson) match {
      case s: JsSuccess[UserPreferences] => s.get
      case e: JsError => throw new IllegalArgumentException(s"Error parsing [$prefsString] as preferences: $e")
    }
    val roles = row.as[String]("roles").split(",").map(x => Role(x.trim)).toSet
    val created = JdbcUtils.toLocalDateTime(row, "created")
    User(id, username, preferences, profiles, roles, created)
  }

  override protected def toDataSeq(u: User) = {
    val prefs = Json.toJson(u.preferences).toString()
    val profiles = u.profiles.map(l => s"${l.providerID}:${l.providerKey}").mkString(", ")
    val roles = u.roles.map(_.toString).mkString(",")
    Seq(u.id, u.username, prefs, profiles, roles, u.created)
  }
}
