package controllers.admin

import akka.util.Timeout
import controllers.BaseController
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.twirl.api.Html
import services.sandbox.SandboxTask
import utils.ApplicationContext

import scala.concurrent.Future
import scala.concurrent.duration._

@javax.inject.Singleton
class SandboxController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  implicit val timeout = Timeout(10.seconds)

  def list = withSession("sandbox-list") { implicit request =>
    Future.successful(Ok(views.html.admin.sandbox.list(request.identity)))
  }

  def sandbox(key: String) = withSession(key) { implicit request =>
    val sandbox = SandboxTask.byId.getOrElse(key, throw new IllegalStateException())
    sandbox.run(ctx).map { result =>
      if (sandbox.isHtml) {
        Ok(views.html.admin.sandbox.view(request.identity, Html(result)))
      } else {
        Ok(result)
      }
    }
  }
}
