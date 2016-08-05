package services.sandbox

import utils.ApplicationContext

import scala.concurrent.Future

object Testbed extends SandboxTask {
  override def id = "testbed"
  override def name = "Testbed"
  override def description = ""

  override def run(ctx: ApplicationContext) = {
    val ret = "Hello!"
    Future.successful(ret)
  }
}
