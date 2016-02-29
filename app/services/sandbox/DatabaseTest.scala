package services.sandbox

import models.database.ConnectionSettings
import services.database.DatabaseService
import utils.ApplicationContext

import scala.concurrent.Future

object DatabaseTest extends SandboxTask {
  override def id = "dbtest"
  override def name = "Database Test"

  override def description = ""

  override def run(ctx: ApplicationContext) = {
    val cs = ConnectionSettings(
      url = "jdbc:h2:./database.h2",
      username = "",
      password = ""
    )
    val ret = DatabaseService.connect(cs)
    Future.successful(ret.toString)
  }
}
