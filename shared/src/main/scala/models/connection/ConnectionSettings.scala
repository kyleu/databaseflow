package models.connection

import java.util.UUID

import models.engine.DatabaseEngine
import models.engine.DatabaseEngine.PostgreSQL
import models.user.Permission

object ConnectionSettings {
  val defaultEngine = PostgreSQL
}

case class ConnectionSettings(
    id: UUID,
    name: String,
    owner: UUID,
    read: Permission = Permission.User,
    edit: Permission = Permission.Private,
    description: String = "",
    engine: DatabaseEngine = ConnectionSettings.defaultEngine,
    host: Option[String] = None,
    port: Option[Int] = None,
    dbName: Option[String] = None,
    extra: Option[String] = None,
    urlOverride: Option[String] = None,
    username: String = "",
    password: String = ""
) {
  val url = urlOverride match {
    case Some(u) => u
    case None => engine.url(host, port, dbName, extra)
  }
}
