package services.connection

import java.util.UUID

import models.audit.AuditType
import models.connection.ConnectionSettings
import models.engine.DatabaseEngine
import models.queries.connection.ConnectionSettingsQueries
import models.user.{Role, User}
import services.audit.AuditRecordService
import services.config.ConfigFileService
import services.database.DatabaseRegistry
import services.database.core.{MasterDatabase, ResultCacheDatabase}

object ConnectionSettingsService {
  def getVisible(user: User) = MasterDatabase.query(ConnectionSettingsQueries.getVisible(user))

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
  def delete(id: UUID, userId: UUID) = {
    MasterDatabase.executeUpdate(ConnectionSettingsQueries.removeById(id))
    AuditRecordService.create(AuditType.DeleteConnection, userId, None, Some(id.toString))
  }

  def createSample(ownerId: UUID) = {
    val connectionId = UUID.randomUUID
    val cs = ConnectionSettings(
      id = connectionId,
      name = "Database Flow Sample Database",
      owner = ownerId,
      engine = DatabaseEngine.SQLite,
      dbName = Some(new java.io.File(ConfigFileService.configDir, "sample.db.sqlite").getAbsolutePath)
    )
    ConnectionSettingsService.insert(cs)
    AuditRecordService.create(AuditType.CreateConnection, ownerId, None, Some(connectionId.toString))
    connectionId
  }
}
