package services.data

import models.connection.ConnectionSettings
import models.engine.rdbms._
import models.query.SavedQuery
import services.connection.ConnectionSettingsService
import services.query.SavedQueryService

object SeedData {
  def insert() = {
    addConnections()
    addSavedQueries()
  }

  private[this] def addConnections() = {
    ConnectionSettingsService.insert(ConnectionSettings(
      name = "PostgreSQL Sample",
      description = "The pagila sample database provided by the community.",
      engine = PostgreSQL, host = Some("localhost"), dbName = Some("pagila"),
      username = "databaseflow", password = "flow"
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      name = "MySQL Sample",
      description = "The salika sample database provided by the community.",
      engine = MySQL, host = Some("localhost"), dbName = Some("salika"),
      username = "root", password = ""
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      name = "Oracle Test",
      description = "The test database for my Oracle VM.",
      engine = Oracle, host = Some("10.211.55.5"), dbName = Some("XE"),
      username = "databaseflow", password = "flow"
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      name = "SQL Server Test",
      description = "The test database for my SQL Server VM.",
      engine = SqlServer, host = Some("10.211.55.5"), dbName = Some("databaseflow"),
      username = "databaseflow", password = "flow"
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      name = "Local H2",
      description = "A scratchpad database to play around in.",
      engine = H2, dbName = Some("~/database.h2db"),
      username = "", password = ""
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      name = "AppThis Local",
      description = "The local database for AppThis v2.",
      engine = MySQL, host = Some("localhost"), dbName = Some("appthis_local"),
      username = "appthis", password = "Mah14Mah1"
    ))
  }

  private[this] def addSavedQueries() = {
    SavedQueryService.save(SavedQuery(name = "Saved Query 1", sql = "select * from stuff1", read = "visitor", edit = "private"))
    SavedQueryService.save(SavedQuery(name = "Saved Query 2", sql = "select * from stuff2", read = "visitor", edit = "private"))
    SavedQueryService.save(SavedQuery(name = "Saved Query 3", sql = "select * from stuff3", read = "visitor", edit = "private"))
  }
}
