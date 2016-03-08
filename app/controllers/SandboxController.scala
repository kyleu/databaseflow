package controllers

import akka.util.Timeout
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.sandbox.{ HtmlSandbox, SandboxTask }
import utils.ApplicationContext

import scala.concurrent.Future
import scala.concurrent.duration._

@javax.inject.Singleton
class SandboxController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  implicit val timeout = Timeout(10.seconds)

  def sandbox(key: String) = withSession(key) { implicit request =>
    val sandbox = SandboxTask.byId.getOrElse(key, throw new IllegalStateException())
    if (sandbox == HtmlSandbox) {
      Future.successful(Ok(views.html.sandbox()))
    } else {
      sandbox.run(ctx).map { result =>
        Ok(result)
      }
    }
  }
}
