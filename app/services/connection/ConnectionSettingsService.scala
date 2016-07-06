package services.connection

import java.util.UUID

import models.connection.ConnectionSettings
import models.queries.connection.ConnectionSettingsQueries
import models.user.{Role, User}
import services.database.MasterDatabase

object ConnectionSettingsService {

  val masterConnectionSettings = ConnectionSettings(
    id = MasterDatabase.connectionId,
    engine = MasterDatabase.engine,
    name = s"${utils.Config.projectName} Storage",
    description = s"Internal storage used by ${utils.Config.projectName}.",
    url = MasterDatabase.url,
    username = MasterDatabase.username,
    password = MasterDatabase.password
  )

  def getAll = MasterDatabase.conn.query(ConnectionSettingsQueries.getAll()) :+ masterConnectionSettings
  def getVisible(user: Option[User]) = MasterDatabase.conn.query(ConnectionSettingsQueries.getVisible(user)) :+ masterConnectionSettings

  def canRead(user: Option[User], cs: ConnectionSettings) = matchPermissions(user, cs, cs.read)
  def canEdit(user: Option[User], cs: ConnectionSettings) = if (cs.id == services.database.MasterDatabase.connectionId) {
    false -> "Cannot edit master database."
  } else {
    matchPermissions(user, cs, cs.edit)
  }

  private[this] def matchPermissions(user: Option[User], cs: ConnectionSettings, perm: String) = user match {
    case Some(u) =>
      if (cs.owner.contains(u.id)) {
        true -> "You are the owner of this connection."
      } else {
        perm match {
          case "admin" => if (u.role == Role.Admin) {
            true -> "Administrators may edit this connection."
          } else {
            false -> "Only administrators are allowed to edit this connection."
          }
          case "user" => if (u.role == Role.Admin || u.role == Role.User) {
            true -> "All normal users may edit this connection."
          } else {
            false -> "Visitors are not allowed to edit this connection."
          }
          case "visitor" => true -> "All users, including visitors, may edit this connection."
          case "private" => if (cs.owner.isDefined) {
            false -> "Only the owner of this connection may edit it."
          } else {
            true -> "Anyone may edit this connection."
          }
          case x => false -> x.toString
        }
      }
    case None =>
      if (cs.owner.isEmpty || cs.edit == "visitor") {
        true -> "Visitors are allowed to edit this connection."
      } else {
        false -> "Visitors are not allowed to edit this connection."
      }
  }

  def getById(id: UUID) = if (id == MasterDatabase.connectionId) {
    Some(masterConnectionSettings)
  } else {
    MasterDatabase.conn.query(ConnectionSettingsQueries.getById(id))
  }

  def insert(connSettings: ConnectionSettings) = MasterDatabase.conn.executeUpdate(ConnectionSettingsQueries.insert(connSettings))
  def update(connSettings: ConnectionSettings) = MasterDatabase.conn.executeUpdate(ConnectionSettingsQueries.Update(connSettings))
  def delete(id: UUID) = MasterDatabase.conn.executeUpdate(ConnectionSettingsQueries.removeById(id))
}
