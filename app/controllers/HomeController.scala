package controllers

import models.queries.connection.ConnectionQueries
import play.api.Mode
import play.api.mvc.Action
import services.database.MasterDatabase
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def index() = withSession("index") { implicit request =>
    val connections = MasterDatabase.db.query(ConnectionQueries.getAll())
    Future.successful(Ok(views.html.index(request.identity, ctx.config.debug, connections)))
  }

  def untrail(path: String) = Action.async {
    Future.successful(MovedPermanently(s"/$path"))
  }

  def externalLink(url: String) = withSession("external.link") { implicit request =>
    Future.successful(Redirect(if (url.startsWith("http")) { url } else { "http://" + url }))
  }

  def ping(timestamp: Long) = withSession("ping") { implicit request =>
    Future.successful(Ok(timestamp.toString))
  }
}
