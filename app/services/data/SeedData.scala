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
      engine = PostgreSQL, url = "jdbc:postgresql://localhost:5432/pagila",
      username = "databaseflow", password = "flow"
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      name = "MySQL Sample",
      description = "The salika sample database provided by the community.",
      engine = MySQL, url = "jdbc:mysql://localhost/sakila",
      username = "root", password = ""
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      name = "Oracle Test",
      description = "The test database for my Oracle VM.",
      engine = Oracle, url = "jdbc:oracle:thin:@//10.211.55.5:1521/XE",
      username = "databaseflow", password = "flow"
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      name = "SQL Server Test",
      description = "The test database for my SQL Server VM.",
      engine = SqlServer, url = "jdbc:sqlserver://10.211.55.5:1433;databaseName=databaseflow",
      username = "databaseflow", password = "flow"
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      name = "Local H2",
      description = "A scratchpad database to play around in.",
      engine = H2, url = "jdbc:h2:~/database.h2db",
      username = "", password = ""
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      name = "AppThis Local",
      description = "The local database for AppThis v2.",
      engine = MySQL, url = "jdbc:mysql://localhost/appthis_local",
      username = "appthis", password = "Mah14Mah1"
    ))
  }

  private[this] def addSavedQueries() = {
    SavedQueryService.save(SavedQuery(name = "Saved Query 1", sql = "select * from stuff1", read = "visitor", edit = "private"))
    SavedQueryService.save(SavedQuery(name = "Saved Query 2", sql = "select * from stuff2", read = "visitor", edit = "private"))
    SavedQueryService.save(SavedQuery(name = "Saved Query 3", sql = "select * from stuff3", read = "visitor", edit = "private"))
  }
}
