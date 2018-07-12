package services.database

import models.database.{Query, Queryable, Row, Statement}
import models.ddl._
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import util.Logging

import scala.util.control.NonFatal

object MasterDdl extends Logging {
  val tables = Seq(
    CreateUsersTable,
    CreatePasswordInfoTable,

    CreateConnectionsTable,
    CreateSavedQueriesTable,

    CreateQueryResultsTable,
    CreateSharedResultTable,

    CreateGraphQLTable,

    CreateSettingsTable,
    CreateAuditRecordTable
  )

  def update(q: Queryable) = {
    tables.foreach { t =>
      val exists = q.query(DdlQueries.DoesTableExist(t.tableName))
      if (!exists) {
        log.info(s"Creating missing table [${t.tableName}].")
        q.executeUpdate(t)
      }
    }
    try {
      q.query(new Query[Unit] {
        override def sql = """select "project_location" from "connections""""
        override def reduce(rows: Iterator[Row]) = ()
      })
    } catch {
      case NonFatal(x) => q.executeUpdate(new Statement {
        override def sql = """alter table "connections" add column "project_location" varchar(1024)"""
      })
    }
  }

  def wipe(q: Queryable) = {
    log.warn("Wiping database schema.")
    val tableNames = tables.reverseMap(_.tableName)
    tableNames.map { tableName =>
      q.executeUpdate(DdlQueries.TruncateTable(tableName))
    }
  }
}
