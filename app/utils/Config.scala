package utils

import java.net.InetAddress

import com.typesafe.config.ConfigFactory
import play.api.Play

object Config {
  private[this] val cnf = ConfigFactory.load()

  val projectId = "databaseflow"
  val projectName = "Database Flow"
  val version = "0.1"
  val hostname = InetAddress.getLocalHost.getHostName
}

@javax.inject.Singleton
class Config @javax.inject.Inject() (val cnf: play.api.Configuration) {
  val debug = !Play.isProd(Play.current)

  val fileCacheDir = cnf.getString("cache.dir").getOrElse("./cache")

  // Admin
  val adminEmail = cnf.getString("admin.email").getOrElse(throw new IllegalStateException("Missing admin email."))

  // Notifications
  val slackEnabled = cnf.getBoolean("slack.enabled").getOrElse(false)
  val slackUrl = cnf.getString("slack.url").getOrElse("no_url_provided")

  // Metrics
  val jmxEnabled = cnf.getBoolean("metrics.jmx.enabled").getOrElse(true)
  val graphiteEnabled = cnf.getBoolean("metrics.graphite.enabled").getOrElse(false)
  val graphiteServer = cnf.getString("metrics.graphite.server").getOrElse("127.0.0.1")
  val graphitePort = cnf.getInt("metrics.graphite.port").getOrElse(2003)
  val servletEnabled = cnf.getBoolean("metrics.servlet.enabled").getOrElse(true)
  val servletPort = cnf.getInt("metrics.servlet.port").getOrElse(9001)
}
