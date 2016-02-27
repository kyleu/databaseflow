package models.engine

import models.engine.rdbms.{H2, Postgres, MySql}

object EngineRegistry {
  val rdbmsEngines = Seq(
    H2.engine,
    MySql.engine,
    Postgres.engine
  )

  val all = rdbmsEngines

  private[this] val enginesById = all.map(x => x.id -> x).toMap

  def get(id: String) = enginesById.getOrElse(id, throw new IllegalArgumentException(s"No database engine registered as [$id]."))
}
