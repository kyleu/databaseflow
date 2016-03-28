package services.engine

import java.util.UUID

import models.engine.ConnectionSettings
import models.engine.rdbms._
import models.queries.connection.ConnectionSettingsQueries
import services.database.MasterDatabase

object ConnectionSettingsService {
  val masterId = UUID.fromString("00000000-0000-0000-0000-000000000000")

  val (masterEngine, masterUrl) = PostgreSQL -> "jdbc:postgresql://localhost:5432/databaseflow?stringtype=unspecified"
  //val (masterEngine, masterUrl) = H2 -> "jdbc:h2:./db/databaseflow"

  val masterUsername = "databaseflow"
  private[this] val masterPassword = "flow"

  val masterConnectionSettings = ConnectionSettings(
    id = masterId,
    engine = masterEngine,
    name = "Database Flow Storage",
    url = masterUrl,
    username = masterUsername,
    password = masterPassword
  )

  def getAll = masterConnectionSettings +: MasterDatabase.db.query(ConnectionSettingsQueries.getAll())

  def getById(id: UUID) = if (id == masterId) {
    Some(masterConnectionSettings)
  } else {
    MasterDatabase.db.query(ConnectionSettingsQueries.getById(id))
  }

  def insert(connSettings: ConnectionSettings) = MasterDatabase.db.execute(ConnectionSettingsQueries.insert(connSettings))
  def update(connSettings: ConnectionSettings) = MasterDatabase.db.execute(ConnectionSettingsQueries.Update(connSettings))
  def delete(id: UUID) = MasterDatabase.db.execute(ConnectionSettingsQueries.delete(id))
}
