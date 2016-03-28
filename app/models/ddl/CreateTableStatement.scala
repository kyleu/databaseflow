package models.ddl

import models.database.Statement
import models.engine.DatabaseEngine
import services.engine.ConnectionSettingsService

abstract class CreateTableStatement(val tableName: String, val eng: DatabaseEngine = ConnectionSettingsService.masterEngine) extends Statement
