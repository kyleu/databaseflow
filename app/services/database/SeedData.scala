package services.database

import java.util.UUID

import models.engine.ConnectionSettings
import models.engine.rdbms.{ H2, MySQL, PostgreSQL }
import models.query.SavedQuery
import services.query.SavedQueryService

object SeedData {
  def insert(db: DatabaseConnection) = {

    // ConnectionSettings
    ConnectionSettingsService.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "PostgreSQL Sample",
      description = "The pagila sample database provided by the community.",
      engine = PostgreSQL,
      url = "jdbc:postgresql://localhost:5432/pagila",
      username = "databaseflow",
      password = "flow"
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "MySQL Sample",
      description = "The salika sample database provided by the community.",
      engine = MySQL,
      url = "jdbc:mysql://localhost/sakila",
      username = "root",
      password = ""
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "Local H2",
      description = "A scratchpad database to play around in.",
      engine = H2,
      url = "jdbc:h2:~/database.h2db",
      username = "",
      password = ""
    ))

    ConnectionSettingsService.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "AppThis Local",
      description = "The local database for AppThis v2.",
      engine = MySQL,
      url = "jdbc:mysql://localhost/appthis_local",
      username = "appthis",
      password = "Mah14Mah1"
    ))

    // Saved Queries
    SavedQueryService.save(SavedQuery(
      name = "Saved Query 1",
      sql = "select * from stuff",
      public = true
    ))

    SavedQueryService.save(SavedQuery(
      name = "Saved Query 2",
      sql = "select * from stuff2",
      public = true
    ))

    SavedQueryService.save(SavedQuery(
      name = "Saved Query 3",
      sql = "select * from stuff3",
      public = true
    ))
  }
}
