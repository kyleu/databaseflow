package services.connection

import java.util.UUID

import models.connection.ConnectionSettings
import models.queries.connection.ConnectionSettingsQueries
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
  def getVisible(userId: Option[UUID]) = MasterDatabase.conn.query(ConnectionSettingsQueries.getVisible(userId)) :+ masterConnectionSettings

  def getById(id: UUID) = if (id == MasterDatabase.connectionId) {
    Some(masterConnectionSettings)
  } else {
    MasterDatabase.conn.query(ConnectionSettingsQueries.getById(id))
  }

  def insert(connSettings: ConnectionSettings) = MasterDatabase.conn.executeUpdate(ConnectionSettingsQueries.insert(connSettings))
  def update(connSettings: ConnectionSettings) = MasterDatabase.conn.executeUpdate(ConnectionSettingsQueries.Update(connSettings))
  def delete(id: UUID) = MasterDatabase.conn.executeUpdate(ConnectionSettingsQueries.removeById(id))
}
