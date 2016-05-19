package services.sandbox

import utils.ApplicationContext

import scala.concurrent.Future

object HtmlSandbox extends SandboxTask {
  override def id = "htmltest"
  override def name = "HTML Test"
  override def description = ""
  override def isHtml = true

  override def run(ctx: ApplicationContext) = {
    Future.successful("HTML!")
  }
}
