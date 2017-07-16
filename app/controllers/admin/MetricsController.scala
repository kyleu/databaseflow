package controllers.admin

import controllers.BaseController
import util.ApplicationContext
import util.FutureUtils.defaultContext

@javax.inject.Singleton
class MetricsController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def showMetrics = withAdminSession("admin-metrics") { implicit request =>
    val url = "http://localhost:4261/metrics?pretty=true"
    val call = ctx.ws.url(url).withHttpHeaders("Accept" -> "application/json")
    val f = call.get()

    f.map { json =>
      Ok(views.html.admin.metrics(request.identity, json.body))
    }
  }
}
