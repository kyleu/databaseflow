package controllers

import models.queries.connection.ConnectionQueries
import play.api.Play
import play.api.mvc.Action
import services.database.MasterDatabase
import utils.ApplicationContext

import scala.concurrent.Future
import scala.util.Random

object HomeController {
  val themes = Seq(
    "red", "pink", "purple", "deep-purple", "indigo", "blue",
    "light-blue", "cyan", "teal", "green", "light-green", "lime",
    "yellow", "amber", "orange", "deep-orange", "brown", "grey", "blue-grey"
  )
}

@javax.inject.Singleton
class HomeController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def index() = act("index") { implicit request =>
    val theme = HomeController.themes(Random.nextInt(HomeController.themes.size))
    //val theme = "blue-grey"

    val connections = MasterDatabase.db.query(ConnectionQueries.getAll())

    Future.successful(Ok(views.html.index(theme, Play.isDev(Play.current), connections)))
  }

  def untrail(path: String) = Action.async {
    Future.successful(MovedPermanently(s"/$path"))
  }

  def externalLink(url: String) = act("external.link") { implicit request =>
    Future.successful(Redirect(if (url.startsWith("http")) { url } else { "http://" + url }))
  }

  def ping(timestamp: Long) = act("ping") { implicit request =>
    Future.successful(Ok(timestamp.toString))
  }
}
