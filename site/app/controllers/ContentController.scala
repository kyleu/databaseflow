package controllers

import akka.actor.ActorSystem
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

@javax.inject.Singleton
class ContentController @javax.inject.Inject() (implicit
  override val messagesApi: MessagesApi, val actorSystem: ActorSystem, val lifecycle: ApplicationLifecycle, val config: Configuration
) extends BaseSiteController {
  def features() = act("features") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.features(isAdmin)))
  }

  def plan() = act("plan") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.plan(isAdmin)))
  }

  def database(key: String) = act(s"db.$key") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.database(util.SiteEngine.withName(key), isAdmin)))
  }

  def technology() = act("technology") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.technology(isAdmin)))
  }

  def privacy() = act("privacy") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.privacy(isAdmin)))
  }

  def robots() = act("robots.txt") { implicit request =>
    Future.successful(Ok("User-agent: *\nDisallow:"))
  }
}
