package services.database

import java.util.UUID

import models.engine.rdbms.{MySql, Postgres}
import models.server.DatabaseServer

import scala.concurrent.Future

@javax.inject.Singleton
class DatabaseService @javax.inject.Inject() () {
  def getServers = {
    Future.successful(Seq(
      DatabaseServer(
        id = UUID.randomUUID,
        name = "Local Postgres",
        engine = Postgres.engine,
        hostname = "localhost"
      ),
      DatabaseServer(
        id = UUID.randomUUID,
        name = "Local MySQL",
        engine = MySql.engine,
        hostname = "localhost"
      )
    ))
  }
}
