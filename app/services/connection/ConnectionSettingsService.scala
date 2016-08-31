package services.connection

import java.util.UUID

import models.connection.ConnectionSettings
import models.queries.connection.ConnectionSettingsQueries
import models.user.{Role, User}
import services.database.DatabaseRegistry
import services.database.core.{MasterDatabase, ResultCacheDatabase}

object ConnectionSettingsService {
  def getAll = MasterDatabase.query(ConnectionSettingsQueries.getAll()) ++ MasterDatabase.settings.toSeq
  def getVisible(user: User) = MasterDatabase.query(ConnectionSettingsQueries.getVisible(user)) ++ MasterDatabase.settings.toSeq

  def canRead(user: User, cs: ConnectionSettings) = Role.matchPermissions(Some(user), cs.owner, "connection", "read", cs.read)
  def canEdit(user: User, cs: ConnectionSettings) = if (cs.id == MasterDatabase.connectionId) {
    false -> "Cannot edit master database."
  } else {
    Role.matchPermissions(Some(user), cs.owner, "connection", "edit", cs.edit)
  }

  def getById(id: UUID) = if (id == MasterDatabase.connectionId) {
    MasterDatabase.settings
  } else if (id == ResultCacheDatabase.connectionId) {
    ResultCacheDatabase.settings
  } else {
    MasterDatabase.query(ConnectionSettingsQueries.getById(id))
  }

  def insert(connSettings: ConnectionSettings) = MasterDatabase.executeUpdate(ConnectionSettingsQueries.insert(connSettings))
  def update(connSettings: ConnectionSettings) = {
    DatabaseRegistry.flush(connSettings.id)
    MasterDatabase.executeUpdate(ConnectionSettingsQueries.Update(connSettings))
  }
  def delete(id: UUID) = MasterDatabase.executeUpdate(ConnectionSettingsQueries.removeById(id))
}
