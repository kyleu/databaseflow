package services.database

import java.util.UUID

import models.engine.ConnectionSettings
import models.engine.rdbms.{ H2, MySQL, PostgreSQL }
import models.queries.connection.ConnectionSettingsQueries
import models.queries.query.SavedQueryQueries
import models.query.SavedQuery
import utils.DateUtils

object SeedData {
  def insert(db: Database) = {

    // ConnectionSettings
    db.execute(ConnectionSettingsQueries.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "PostgreSQL Sample",
      description = "The pagila sample database provided by the community.",
      engine = PostgreSQL,
      url = "jdbc:postgresql://localhost:5432/pagila",
      username = "databaseflow",
      password = "flow"
    )))

    db.execute(ConnectionSettingsQueries.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "MySQL Sample",
      description = "The salika sample database provided by the community.",
      engine = MySQL,
      url = "jdbc:mysql://localhost/sakila",
      username = "root",
      password = ""
    )))

    db.execute(ConnectionSettingsQueries.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "Local H2",
      description = "A scratchpad database to play around in.",
      engine = H2,
      url = "jdbc:h2:~/database.h2db",
      username = "",
      password = ""
    )))

    db.execute(ConnectionSettingsQueries.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "AppThis Local",
      description = "The local database for AppThis v2.",
      engine = MySQL,
      url = "jdbc:mysql://localhost/appthis_local",
      username = "appthis",
      password = "Mah14Mah1"
    )))

    // Saved Queries
    db.execute(SavedQueryQueries.insert(SavedQuery(
      id = UUID.randomUUID,
      name = "Saved Query 1",
      sql = "select * from stuff"
    )))

    db.execute(SavedQueryQueries.insert(SavedQuery(
      id = UUID.randomUUID,
      name = "Saved Query 2",
      sql = "select * from stuff2"
    )))

    db.execute(SavedQueryQueries.insert(SavedQuery(
      id = UUID.randomUUID,
      name = "Saved Query 3",
      sql = "select * from stuff3"
    )))
  }
}
