package controllers

import akka.actor.ActorSystem
import com.codahale.metrics.SharedMetricRegistries
import play.api.Configuration
import play.api.i18n.{Lang, MessagesApi}
import play.api.inject.ApplicationLifecycle
import util.metrics.{MetricsConfig, MetricsServletActor}

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
class SiteController @javax.inject.Inject() (
    implicit
    override val messagesApi: MessagesApi, val actorSystem: ActorSystem, val lifecycle: ApplicationLifecycle, val config: Configuration
) extends BaseSiteController {
  val metricsConfig: MetricsConfig = MetricsConfig(
    jmxEnabled = config.get[Option[Boolean]]("metrics.jmx.enabled").getOrElse(true),
    graphiteEnabled = config.get[Option[Boolean]]("metrics.graphite.enabled").getOrElse(false),
    graphiteServer = config.get[Option[String]]("metrics.graphite.server").getOrElse("127.0.0.1"),
    graphitePort = config.get[Option[Int]]("metrics.graphite.port").getOrElse(2003),
    servletEnabled = config.get[Option[Boolean]]("metrics.servlet.enabled").getOrElse(true),
    servletPort = config.get[Option[Int]]("metrics.servlet.port").getOrElse(9001)
  )

  actorSystem.actorOf(MetricsServletActor.props(metricsConfig), "metrics-servlet")
  lifecycle.addStopHook(() => Future.successful(SharedMetricRegistries.remove("default")))

  def index() = act("index") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.index(isAdmin)).withHeaders(SiteController.cors: _*))
  }

  def language(lang: String) = act("language") { implicit request =>
    val l = Lang(lang)
    val result = Redirect(controllers.routes.SiteController.index()).withLang(l)
    Future.successful(if (lang == "en") {
      result
    } else {
      result.flashing("lang" -> lang)
    })
  }
}
