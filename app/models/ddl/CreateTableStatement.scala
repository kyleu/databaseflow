package models.ddl

import models.database.Statement
import models.engine.DatabaseEngine
import models.engine.rdbms.PostgreSQL
import services.database.core.MasterDatabase

abstract class CreateTableStatement(val tableName: String, val eng: DatabaseEngine = {
  MasterDatabase.settings.map(_.engine).getOrElse(throw new IllegalStateException("Missing master database engine."))
}) extends Statement {
  protected[this] def varchar = eng match {
    case PostgreSQL => "character varying"
    case _ => "varchar"
  }
}
