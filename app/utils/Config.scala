package utils

import com.typesafe.config.ConfigFactory

object Config {
  private[this] val cnf = ConfigFactory.load()

  val projectId = "databaseflow"
  val projectName = "Database Flow"
  val version = "0.1"

  // Metrics
  val jmxEnabled = cnf.getBoolean("metrics.jmx.enabled")
  val graphiteEnabled = cnf.getBoolean("metrics.graphite.enabled")
  val graphiteServer = cnf.getString("metrics.graphite.server")
  val graphitePort = cnf.getInt("metrics.graphite.port")
  val servletEnabled = cnf.getBoolean("metrics.servlet.enabled")
  val servletPort = cnf.getInt("metrics.servlet.port")
}
