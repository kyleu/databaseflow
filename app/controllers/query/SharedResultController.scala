package controllers.query

import java.util.UUID

import controllers.BaseController
import models.query.RowDataOptions
import models.user.Permission
import services.query.SharedResultService
import services.user.UserService
import utils.ApplicationContext
import utils.web.FormUtils

import scala.concurrent.Future

@javax.inject.Singleton
class SharedResultController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def index() = withoutSession("result.index") { implicit request =>
    val (myResults, publicResults) = SharedResultService.getAll.partition(r => request.identity.exists(_.id == r.owner))
    val usernameMap = UserService.instance.getOrElse(throw new IllegalStateException()).usernameLookupMulti(publicResults.map(_.owner).toSet)
    Future.successful(Ok(views.html.result.list(request.identity, myResults, publicResults, usernameMap)))
  }

  def view(id: UUID) = withoutSession("result.view") { implicit request =>
    SharedResultService.getById(id) match {
      case Some(sr) =>
        val perm = SharedResultService.canView(request.identity, sr)
        if (perm._1) {
          val results = SharedResultService.getData(request.identity, sr, RowDataOptions())
          val ownerName = UserService.instance.getOrElse(throw new IllegalStateException()).usernameLookup(sr.owner)
          sr.chart match {
            case Some(_) => Future.successful(
              Ok(views.html.result.viewChart(request.identity, sr, ownerName.getOrElse("guest"), results.cols, results.data, ctx.config.debug))
            )
            case None => Future.successful(
              Ok(views.html.result.viewData(request.identity, sr, ownerName.getOrElse("guest"), results.cols, results.data))
            )
          }
        } else {
          Future.successful(BadRequest(s"You do not have permission to view the results you requested. ${perm._2}"))
        }
      case None => Future.successful(BadRequest("We couldn't find the results you requested."))
    }
  }

  def save() = withSession("result.save") { implicit request =>
    val form = FormUtils.getForm(request)
    val id = UUID.fromString(form("id"))
    SharedResultService.getById(id) match {
      case Some(sr) =>
        val title = form("title")
        val description = form("description").trim match {
          case x if x.isEmpty => None
          case x => Some(x)
        }
        val chart = form("chart").trim match {
          case x if x.isEmpty => None
          case x => Some(x)
        }
        val share = Permission.withName(form("share"))
        val newSr = sr.copy(title = title, description = description, chart = chart, viewableBy = share)
        SharedResultService.save(request.identity.id, newSr, None)
        Future.successful(Redirect(routes.SharedResultController.view(id)).flashing("success" -> "Shared result saved."))
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
