package services.database

import models.ddl._
import utils.Logging

object MasterSchema extends Logging {
  val tables = Seq(
    CreateUsersTable,
    CreateUserProfilesTable,
    CreateSessionInfoTable,
    CreatePasswordInfoTable,

    CreateConnectionsTable,
    CreateAdHocQueriesTable
  )

  def update(db: Database) = {
    tables.foreach { t =>
      val exists = db.query(DdlQueries.DoesTableExist(t.tableName))
      if (exists) {
        Unit
      } else {
        log.info(s"Creating missing table [${t.tableName}].")
        val name = s"CreateTable-${t.tableName}"
        db.execute(t)
      }
    }
  }

  def wipe(db: Database) = {
    log.warn("Wiping database schema.")
    val tableNames = tables.reverse.map(_.tableName)
    db.execute(DdlQueries.TruncateTables(tableNames))
  }
}
