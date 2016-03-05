package models.ddl

import models.database.Statement
import models.engine.DatabaseEngine
import services.database.MasterDatabase

abstract class CreateTableStatement(val tableName: String, val eng: DatabaseEngine = MasterDatabase.engine) extends Statement
