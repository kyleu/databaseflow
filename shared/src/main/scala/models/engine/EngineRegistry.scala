package models.engine

import models.engine.rdbms.{ Postgres, MySql }

object EngineRegistry {
  private[this] val standardEngines = Seq(
    MySql.engine,
    Postgres.engine
  )

  val all = standardEngines

  private[this] val enginesById = all.map(x => x.id -> x).toMap

  def get(id: String) = enginesById.getOrElse(id, throw new IllegalArgumentException(s"No database engine registered as [$id]."))
}
