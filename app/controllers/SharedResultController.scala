package controllers

import java.util.UUID

import services.query.SharedResultService
import services.user.UserService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class SharedResultController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def index() = withoutSession("result.index") { implicit request =>
    val (myResults, publicResults) = SharedResultService.getAll.partition(r => request.identity.exists(_.id == r.owner))
    val usernameMap = UserService.instance.getOrElse(throw new IllegalStateException()).usernameLookupMulti(publicResults.map(_.owner).toSet)
    Future.successful(Ok(views.html.result.list(request.identity, myResults, publicResults, usernameMap, ctx.config.debug)))
  }

  def view(id: UUID) = withoutSession("result.view") { implicit request =>
    SharedResultService.getById(id) match {
      case Some(sr) =>
        val results = SharedResultService.getData(request.identity, sr)
        val ownerName = UserService.instance.getOrElse(throw new IllegalStateException()).usernameLookup(sr.owner)
        sr.chart match {
          case Some(chart) =>
            Future.successful(Ok(views.html.result.viewChart(request.identity, sr, ownerName.getOrElse("guest"), results.cols, results.data, ctx.config.debug)))
          case None =>
            Future.successful(Ok(views.html.result.viewData(request.identity, sr, ownerName.getOrElse("guest"), results.cols, results.data, ctx.config.debug)))
        }
      case None => Future.successful(BadRequest("We couldn't find the results you requested."))
    }
  }

  def remove(id: UUID) = withSession("result.remove") { implicit request =>
    SharedResultService.getById(id) match {
      case Some(sr) =>
        SharedResultService.delete(sr.id, request.identity.id)
        Future.successful(Redirect(routes.SharedResultController.index()).flashing("success" -> "Shared result removed."))
      case None => Future.successful(BadRequest("We couldn't find the results you requested."))
    }
  }
}
