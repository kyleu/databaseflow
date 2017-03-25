package services.connection

import java.util.UUID

import models.audit.AuditType
import models.connection.ConnectionSettings
import models.queries.connection.ConnectionSettingsQueries
import models.user.{Role, User}
import services.audit.AuditRecordService
import services.database.DatabaseRegistry
import services.database.core.{MasterDatabase, ResultCacheDatabase}

object ConnectionSettingsService {
  def getVisible(user: User, id: Option[UUID] = None, name: Option[String] = None) = {
    val c = MasterDatabase.query(ConnectionSettingsQueries.getVisible(user))
    id match {
      case Some(i) => c.filter(_.id == i)
      case None => name match {
        case Some(n) => c.filter(_.name == n)
        case None => c
      }
    }
  }

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

  def getBySlug(slug: String) = if (slug == MasterDatabase.slug) {
    MasterDatabase.settings
  } else if (slug == ResultCacheDatabase.slug) {
    ResultCacheDatabase.settings
  } else {
    MasterDatabase.query(ConnectionSettingsQueries.GetBySlug(slug))
  }

  def insert(connSettings: ConnectionSettings) = {
    MasterDatabase.query(ConnectionSettingsQueries.GetExisting(connSettings.id, connSettings.name, connSettings.slug)) match {
      case Some(c) => throw new IllegalStateException(s"There is already a connection named [${connSettings.name}].")
      case None => MasterDatabase.executeUpdate(ConnectionSettingsQueries.insert(connSettings))
    }
  }
  def update(connSettings: ConnectionSettings) = {
    MasterDatabase.query(ConnectionSettingsQueries.GetExisting(connSettings.id, connSettings.name, connSettings.slug)) match {
      case Some(c) => throw new IllegalStateException(s"There is already a connection named [${connSettings.name}].")
      case None =>
        DatabaseRegistry.flush(connSettings.id)
        MasterDatabase.executeUpdate(ConnectionSettingsQueries.Update(connSettings))
    }
  }
  def delete(id: UUID, userId: UUID) = {
    MasterDatabase.executeUpdate(ConnectionSettingsQueries.removeById(id))
    AuditRecordService.create(AuditType.DeleteConnection, userId, None, Some(id.toString))
  }
}
