package services.config

import com.typesafe.config.Config
import models.engine.DatabaseEngine
import models.engine.DatabaseEngine.H2

import scala.util.control.NonFatal

object DatabaseConfig {
  def fromConfig(cfg: Config) = {
    val engine = try {
      DatabaseEngine.withName(cfg.getString("db"))
    } catch {
      case NonFatal(x) => H2
    }
    val url = try {
      cfg.getString("url")
    } catch {
      case NonFatal(x) => "default"
    }
    val username = try {
      cfg.getString("username")
    } catch {
      case NonFatal(x) => "databaseflow"
    }
    val password = try {
      cfg.getString("password")
    } catch {
      case NonFatal(x) => "flow"
    }
    DatabaseConfig(engine, url, username, password)
  }
}

case class DatabaseConfig(engine: DatabaseEngine, url: String, username: String, password: String)
