package services.sandbox

import models.queries.connection.ConnectionSettingsQueries
import services.database.MasterDatabase
import services.schema.SchemaService
import utils.ApplicationContext

import scala.concurrent.Future
import scala.util.{ Failure, Success }

object Testbed extends SandboxTask {
  override def id = "testbed"
  override def name = "Testbed"
  override def description = ""

  override def run(ctx: ApplicationContext) = {
    val connections = MasterDatabase.conn.query(ConnectionSettingsQueries.getAll())

    val connection = connections.find(_.name.endsWith("Production")).getOrElse(throw new IllegalStateException())
    val db = MasterDatabase.databaseFor(connection.id) match {
      case Right(d) => d
      case Left(x) => throw x
    }
    val schema = SchemaService.getSchema(db)

    val tableStrings = schema match {
      case Success(s) => s.tables.map(_.name)
      case Failure(x) => throw x
    }
    val prelude = "#!/bin/bash\nset -x\n"
    val ret = prelude +
      tableStrings.sorted.map(s => s"mysqldump --single-transaction -h prod-db-01.appthis.com -uroot -pMah14Mah1 appthis_v2 $s > $s.sql").mkString("\n")
    Future.successful(ret)
  }
}
