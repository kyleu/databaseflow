package services.sandbox

import models.engine.rdbms.H2
import models.engine.{EngineRegistry, DatabaseEngine}
import services.database.DatabaseService
import utils.ApplicationContext

import scala.concurrent.Future

object DatabaseTest extends SandboxTask {
  override def id = "dbtest"
  override def name = "Database Test"

  override def description = ""

  override def run(ctx: ApplicationContext) = {
    val ret = DatabaseService.openConnection(H2.engine, "jdbc:h2:~/database.h2db")
    Future.successful(ret.toString)
  }
}
