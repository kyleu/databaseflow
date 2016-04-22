package services.data

import models.ddl._
import services.database.DatabaseConnection
import utils.Logging

object MasterDdl extends Logging {
  val tables = Seq(
    CreateUsersTable,
    CreatePasswordInfoTable,

    CreateConnectionsTable,
    CreateSavedQueriesTable,

    CreateAuditRecordTable
  )

  def update(db: DatabaseConnection) = {
    tables.foreach { t =>
      val exists = db.query(DdlQueries.DoesTableExist(t.tableName))
      if (exists) {
        Unit
      } else {
        log.info(s"Creating missing table [${t.tableName}].")
        db.executeUpdate(t)
      }
    }
  }

  def wipe(db: DatabaseConnection) = {
    log.warn("Wiping database schema.")
    val tableNames = tables.reverse.map(_.tableName)
    tableNames.map { tableName =>
      db.executeUpdate(DdlQueries.TruncateTable(tableName))
    }
  }
}
