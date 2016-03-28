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
    db.execute(ConnectionSettingsQueries.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "PostgreSQL Sample",
      engine = PostgreSQL,
      url = "jdbc:postgresql://localhost:5432/pagila",
      username = "databaseflow",
      password = "flow"
    )))

    db.execute(ConnectionSettingsQueries.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "MySQL Sample",
      engine = MySQL,
      url = "jdbc:mysql://localhost/sakila",
      username = "root",
      password = ""
    )))

    db.execute(ConnectionSettingsQueries.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "Local H2",
      engine = H2,
      url = "jdbc:h2:~/database.h2db",
      username = "",
      password = ""
    )))

    db.execute(ConnectionSettingsQueries.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "AppThis Local",
      engine = MySQL,
      url = "jdbc:mysql://localhost/appthis_local",
      username = "appthis",
      password = "Mah14Mah1"
    )))

    db.execute(SavedQueryQueries.insert(SavedQuery(
      id = UUID.randomUUID,
      owner = None,
      title = "Saved Query 1",
      sql = "select * from stuff",
      lastRan = None,
      created = DateUtils.nowMillis,
      updated = DateUtils.nowMillis
    )))

    db.execute(SavedQueryQueries.insert(SavedQuery(
      id = UUID.randomUUID,
      owner = None,
      title = "Saved Query 2",
      sql = "select * from stuff2",
      lastRan = None,
      created = DateUtils.nowMillis,
      updated = DateUtils.nowMillis
    )))

    db.execute(SavedQueryQueries.insert(SavedQuery(
      id = UUID.randomUUID,
      owner = None,
      title = "Saved Query 3",
      sql = "select * from stuff3",
      lastRan = None,
      created = DateUtils.nowMillis,
      updated = DateUtils.nowMillis
    )))
  }
}
