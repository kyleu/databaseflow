package services.sandbox

import utils.ApplicationContext

import scala.concurrent.Future

object DatabaseTest extends SandboxTask {
  override def id = "dbtest"
  override def name = "Database Test"

  override def description = ""

  override def run(ctx: ApplicationContext) = {
    Future.successful("DB OK!")
  }
}
