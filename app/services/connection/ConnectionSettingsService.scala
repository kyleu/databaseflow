package services.connection

import java.util.UUID

import models.connection.ConnectionSettings
import models.engine.rdbms._
import models.queries.connection.ConnectionSettingsQueries
import services.database.MasterDatabase

object ConnectionSettingsService {
  //val (masterEngine, masterUrl) = PostgreSQL -> "jdbc:postgresql://localhost:5432/databaseflow?stringtype=unspecified"
  val (masterEngine, masterUrl) = H2 -> "jdbc:h2:./tmp/databaseflow-master"

  val masterUsername = "databaseflow"
  private[this] val masterPassword = "flow"

  val masterConnectionSettings = ConnectionSettings(
    id = MasterDatabase.connectionId,
    engine = masterEngine,
    name = s"${utils.Config.projectName} Storage",
    description = s"Internal storage used by ${utils.Config.projectName}.",
    url = masterUrl,
    username = masterUsername,
    password = masterPassword
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
