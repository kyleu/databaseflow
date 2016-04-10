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
    exampleUrl: String,

    builtInFunctions: Seq[String] = Nil,
    columnTypes: Seq[String] = Nil
) {
  def varchar: String = "?"
  def quoteIdentifier: String = ""

  val explainSupported = true
  def explain(sql: String) = ""

  val analyzeSupported = true
  def analyze(sql: String) = ""

  val showCreateSupported = true
  def showCreateTable(name: String) = ""
  def showCreateView(name: String) = ""

  override def toString = id
}
