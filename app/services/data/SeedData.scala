package services.data

import java.util.UUID

import models.connection.ConnectionSettings
import models.engine.DatabaseEngine._
import models.user.User
import services.connection.ConnectionSettingsService

object SeedData {
  def insert() = {
    ConnectionSettingsService.insert(ConnectionSettings(
      id = UUID.randomUUID, name = "PostgreSQL Sample", owner = User.mock.id,
      description = "The pagila sample database provided by the community.",
      engine = PostgreSQL, host = Some("localhost"), dbName = Some("pagila"),
      username = "databaseflow", password = "flow"
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      id = UUID.randomUUID, name = "MySQL Sample", owner = User.mock.id,
      description = "The salika sample database provided by the community.",
      engine = MySQL, host = Some("localhost"), dbName = Some("salika"),
      username = "root", password = ""
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      id = UUID.randomUUID, name = "Oracle Test", owner = User.mock.id,
      description = "The test database for my Oracle VM.",
      engine = Oracle, host = Some("10.211.55.5"), dbName = Some("XE"),
      username = "databaseflow", password = "flow"
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      id = UUID.randomUUID, name = "SQL Server Test", owner = User.mock.id,
      description = "The test database for my SQL Server VM.",
      engine = SQLServer, host = Some("10.211.55.5"), dbName = Some("databaseflow"),
      username = "databaseflow", password = "flow"
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      id = UUID.randomUUID, name = "Local H2", owner = User.mock.id,
      description = "A scratchpad database to play around in.",
      engine = H2, dbName = Some("~/database.h2db")
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      id = UUID.randomUUID, name = "Local SQLite", owner = User.mock.id,
      description = "A scratchpad database to play around in.",
      engine = SQLite, dbName = Some("~/database.sqlite")
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      id = UUID.randomUUID, name = "AppThis Local", owner = User.mock.id,
      description = "The local database for AppThis v2.",
      engine = MySQL, host = Some("localhost"), dbName = Some("appthis_local"),
      username = "appthis", password = "Mah14Mah1"
    ))
  }
}
