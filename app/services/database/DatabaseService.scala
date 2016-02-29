package services.database

import models.engine.{DatabaseEngine, EngineRegistry}

object DatabaseService {
  def init() = {
    EngineRegistry.all.foreach { r =>
      Class.forName(r.className)
    }
  }

  def openConnection(engine: DatabaseEngine, url: String) = {
    engine.id
  }
}
