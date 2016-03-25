package services.database

import java.util.UUID

import models.engine.ConnectionSettings
import models.engine.rdbms.{ H2, MySQL, PostgreSQL }
import models.queries.connection.ConnectionQueries

object SeedData {
  def insert(db: Database) = {
    db.execute(ConnectionQueries.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "Master Database",
      engine = PostgreSQL,
      url = "jdbc:postgresql://localhost:5432/databaseflow",
      username = "databaseflow",
      password = "flow"
    )))

    db.execute(ConnectionQueries.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "PostgreSQL Sample",
      engine = PostgreSQL,
      url = "jdbc:postgresql://localhost:5432/pagila",
      username = "databaseflow",
      password = "flow"
    )))

    db.execute(ConnectionQueries.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "MySQL Sample",
      engine = MySQL,
      url = "jdbc:mysql://localhost/sakila",
      username = "root",
      password = ""
    )))

    db.execute(ConnectionQueries.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "Local H2",
      engine = H2,
      url = "jdbc:h2:~/database.h2db",
      username = "",
      password = ""
    )))

    db.execute(ConnectionQueries.insert(ConnectionSettings(
      id = UUID.randomUUID,
      name = "AppThis Local",
      engine = MySQL,
      url = "jdbc:mysql://localhost/appthis_local",
      username = "appthis",
      password = "Mah14Mah1"
    )))
  }
}
