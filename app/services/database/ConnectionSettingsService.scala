package services.database

import java.util.UUID

import models.engine.ConnectionSettings
import models.engine.rdbms._
import models.queries.connection.ConnectionSettingsQueries

object ConnectionSettingsService {
  val masterId = UUID.fromString("00000000-0000-0000-0000-000000000000")

  val (masterEngine, masterUrl) = PostgreSQL -> "jdbc:postgresql://localhost:5432/databaseflow?stringtype=unspecified"
  //val (masterEngine, masterUrl) = H2 -> "jdbc:h2:./db/databaseflow"

  val masterUsername = "databaseflow"
  private[this] val masterPassword = "flow"

  val masterConnectionSettings = ConnectionSettings(
    id = masterId,
    engine = masterEngine,
    name = s"${utils.Config.projectName} Storage",
    description = s"Internal storage used by ${utils.Config.projectName}.",
    url = masterUrl,
    username = masterUsername,
    password = masterPassword
  )

  def getAll = MasterDatabase.conn.query(ConnectionSettingsQueries.getAll()) :+ masterConnectionSettings

  def getById(id: UUID) = if (id == masterId) {
    Some(masterConnectionSettings)
  } else {
    MasterDatabase.conn.query(ConnectionSettingsQueries.getById(id))
  }

  def insert(connSettings: ConnectionSettings) = MasterDatabase.conn.execute(ConnectionSettingsQueries.insert(connSettings))
  def update(connSettings: ConnectionSettings) = MasterDatabase.conn.execute(ConnectionSettingsQueries.Update(connSettings))
  def delete(id: UUID) = MasterDatabase.conn.execute(ConnectionSettingsQueries.delete(id))
}
