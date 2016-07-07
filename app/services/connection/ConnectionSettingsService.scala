package services.connection

import java.util.UUID

import models.connection.ConnectionSettings
import models.queries.connection.ConnectionSettingsQueries
import models.user.{Role, User}
import services.database.{MasterDatabase, MasterDatabaseConnection}

object ConnectionSettingsService {
  def getAll = MasterDatabaseConnection.query(ConnectionSettingsQueries.getAll()) ++ MasterDatabaseConnection.settings.toSeq
  def getVisible(user: Option[User]) = MasterDatabaseConnection.query(ConnectionSettingsQueries.getVisible(user)) ++ MasterDatabaseConnection.settings.toSeq

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
            true -> s"Administrators may $perm this connection."
          } else {
            false -> s"Only administrators are allowed to $perm this connection."
          }
          case "user" => if (u.role == Role.Admin || u.role == Role.User) {
            true -> s"All normal users may $perm this connection."
          } else {
            false -> s"Visitors are not allowed to $perm this connection."
          }
          case "visitor" => true -> s"All users, including visitors, may $perm this connection."
          case "private" => if (cs.owner.isDefined) {
            false -> s"Only the owner of this connection may $perm it."
          } else {
            true -> s"Anyone may $perm this connection."
          }
          case x => false -> x.toString
        }
      }
    case None =>
      if (cs.owner.isEmpty || cs.edit == "visitor") {
        true -> s"Visitors are allowed to $perm this connection."
      } else {
        false -> s"Visitors are not allowed to $perm this connection."
      }
  }

  def getById(id: UUID) = if (id == MasterDatabase.connectionId) {
    MasterDatabaseConnection.settings
  } else {
    MasterDatabaseConnection.query(ConnectionSettingsQueries.getById(id))
  }

  def insert(connSettings: ConnectionSettings) = MasterDatabaseConnection.executeUpdate(ConnectionSettingsQueries.insert(connSettings))
  def update(connSettings: ConnectionSettings) = {
    MasterDatabase.flush(connSettings.id)
    MasterDatabaseConnection.executeUpdate(ConnectionSettingsQueries.Update(connSettings))
  }
  def delete(id: UUID) = MasterDatabaseConnection.executeUpdate(ConnectionSettingsQueries.removeById(id))
}
