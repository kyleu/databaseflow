package models.ddl

import models.database.Statement
import models.engine.DatabaseEngine
import services.database.core.MasterDatabase

abstract class CreateTableStatement(val tableName: String, val eng: DatabaseEngine = {
  MasterDatabase.settings.map(_.engine).getOrElse(throw new IllegalStateException("Missing master database engine."))
}) extends Statement
