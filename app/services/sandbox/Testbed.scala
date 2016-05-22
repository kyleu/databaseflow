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
    val db = MasterDatabase.db(connection.id)
    val schema = SchemaService.getSchema(db)

    val tableStrings = schema match {
      case Success(s) => s.tables.map(_.name)
      case Failure(x) => throw x
    }
    val prelude = "#!/bin/bash\nset -x\n"
    val options = "--create-options --compress --compact --allow-keywords --single-transaction"
    val ret = prelude +
      tableStrings.sorted.map { s =>
        s"mysqldump --no-data $options -h prod-db-01.appthis.com -uroot -pMah14Mah1 appthis_v2 $s > dump/$s-ddl.sql\n" +
          s"mysqldump --no-create-info $options -h prod-db-01.appthis.com -uroot -pMah14Mah1 appthis_v2 $s > dump/$s-data.sql\n" +
          s"sed -i 's/DEFINER=`root`@`96.88.76.49`/DEFINER=`root`@`127.0.0.1`/g' dump/$s-ddl.sql\n"
      }.mkString("\n")
    Future.successful(ret)
  }
}
