package controllers

import akka.actor.ActorSystem
import com.codahale.metrics.SharedMetricRegistries
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.ApplicationLifecycle
import play.api.mvc.{Action, Controller}
import utils.metrics.{MetricsConfig, MetricsServletActor}

import scala.concurrent.Future

object SiteController {
  val cors = Seq(
    "Access-Control-Allow-Headers" -> "Content-Type,x-requested-with,Authorization,Access-Control-Allow-Origin",
    "Access-Control-Allow-Methods" -> "GET,POST,OPTIONS",
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Max-Age" -> "360"
  )
}

@javax.inject.Singleton
class SiteController @javax.inject.Inject() (implicit
  val messagesApi: MessagesApi,
    val actorSystem: ActorSystem,
    val lifecycle: ApplicationLifecycle) extends BaseSiteController {

  val metricsConfig = MetricsConfig(jmxEnabled = true, graphiteEnabled = false, "127.0.0.1", 2003, servletEnabled = true, 9001)
  actorSystem.actorOf(MetricsServletActor.props(metricsConfig), "metrics-servlet")
  lifecycle.addStopHook(() => Future.successful(SharedMetricRegistries.remove("default")))

  def splash() = act("splash") { implicit request =>
    Future.successful(Ok(views.html.splash()).withHeaders(SiteController.cors: _*))
  }

  def index() = act("index") { implicit request =>
    Future.successful(Ok(views.html.index()).withHeaders(SiteController.cors: _*))
  }

  def robots() = act("robots-txt") { implicit request =>
    Future.successful(Ok("User-agent: *\nDisallow:"))
  }
}
