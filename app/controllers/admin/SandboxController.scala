package controllers.admin

import akka.util.Timeout
import controllers.BaseController
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.twirl.api.Html
import services.sandbox.SandboxTask
import utils.ApplicationContext

import scala.concurrent.Future
import scala.concurrent.duration._

@javax.inject.Singleton
class SandboxController @javax.inject.Inject() (override val ctx: ApplicationContext, ws: WSClient) extends BaseController {
  implicit val timeout = Timeout(10.seconds)

  def list = withAdminSession("sandbox.list") { implicit request =>
    Future.successful(Ok(views.html.admin.sandbox.list(request.identity)))
  }

  def sandbox(key: String) = withAdminSession("sandbox." + key) { implicit request =>
    val sandbox = SandboxTask.byId.getOrElse(key, throw new IllegalStateException())
    sandbox.run(ctx).map { result =>
      if (sandbox.isHtml) {
        Ok(views.html.admin.sandbox.view(request.identity, Html(result)))
      } else {
        Ok(result)
      }
    }
  }

  def dumpMetrics() = withAdminSession("sandbox.metrics") { implicit request =>
    val url = "http://localhost:4261/metrics?pretty=true"
    val call = ws.url(url).withHeaders("Accept" -> "application/json")
    val f = call.get()

    f.map { json =>
      Ok(views.html.admin.sandbox.metrics(request.identity, json.body))
    }
  }
}
