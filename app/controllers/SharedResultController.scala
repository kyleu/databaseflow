package controllers

import java.util.UUID

import services.query.SharedResultService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class SharedResultController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def index() = withoutSession("result.index") { implicit request =>
    Future.successful(Ok(views.html.result.list(request.identity, SharedResultService.getAll, ctx.config.debug)))
  }

  def view(id: UUID) = withoutSession("result.view") { implicit request =>
    SharedResultService.getById(id) match {
      case Some(sr) =>
        val results = SharedResultService.getData(request.identity, sr)
        sr.chart match {
          case Some(chart) => Future.successful(Ok(views.html.result.viewChart(request.identity, sr, results.cols, results.data, ctx.config.debug)))
          case None => Future.successful(Ok(views.html.result.viewData(request.identity, sr, results.cols, results.data, ctx.config.debug)))
        }
      case None => Future.successful(BadRequest("We couldn't find the results you requested."))
    }
  }
}
