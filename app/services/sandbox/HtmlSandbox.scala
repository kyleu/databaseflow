package services.sandbox

import utils.ApplicationContext

object HtmlSandbox extends SandboxTask {
  override def id = "htmltest"
  override def name = "HTML Test"
  override def description = ""

  override def run(ctx: ApplicationContext) = throw new IllegalStateException("Not meant to be run directly.")
}
