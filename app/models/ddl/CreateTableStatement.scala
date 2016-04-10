package models.ddl

import models.database.Statement
import models.engine.DatabaseEngine
import models.engine.rdbms.PostgreSQL
import services.database.ConnectionSettingsService

abstract class CreateTableStatement(val tableName: String, val eng: DatabaseEngine = ConnectionSettingsService.masterEngine) extends Statement {
  protected[this] def varchar = eng match {
    case PostgreSQL => "character varying"
    case _ => "varchar"
  }
}
