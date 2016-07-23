package models.engine

import models.engine.rdbms._

object DatabaseEngine {
  val rdbmsEngines = Seq(H2, MySQL, Oracle, PostgreSQL, SqlServer)

  val all = rdbmsEngines

  private[this] val enginesById = all.map(x => x.id -> x).toMap

  def get(id: String) = enginesById.getOrElse(id, throw new IllegalArgumentException(s"No database engine registered as [$id]."))
}

case class DatabaseEngine(
    id: String,
    name: String,
    driverClass: String,
    defaultPort: Int,
    exampleUrl: String
) {
  def builtInFunctions: Seq[String] = Nil
  def columnTypes: Seq[String] = Nil

  def leftQuoteIdentifier: String = "\""
  def rightQuoteIdentifier: String = "\""

  def explain: Option[(String) => String] = None
  def analyze: Option[(String) => String] = None

  def url(host: Option[String], port: Option[Int], dbName: Option[String], extra: Option[String]) = exampleUrl

  override def toString = id
}
