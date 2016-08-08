package controllers

import java.net.URL

import play.api.i18n.Messages
import play.api.mvc.Action
import services.connection.ConnectionSettingsService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def home() = withSession("home") { implicit request =>
    val connections = ConnectionSettingsService.getVisible(request.identity)
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

  def robots() = withSession("robots") { implicit request =>
    Future.successful(Ok("User-agent: *\nDisallow: /"))
  }

  private[this] def parseMsgs(url: URL) = Messages.parse(Messages.UrlMessageSource(url), url.toString).fold(e => throw e, identity)

  private[this] lazy val msgs = Seq("en", "ar", "de", "es", "fr", "hi", "pt", "zh").map { l =>
    l -> parseMsgs(getClass.getClassLoader.getResource(s"client/messages.$l"))
  }.toMap

  private[this] val responses = msgs.map { ms =>
    val vals = ms._2.map { m =>
      s""""${m._1}": "${m._2}""""
    }.mkString(",\n  ")
    ms._1 -> s"""window.messages = {\n  $vals\n}"""
  }

  def strings() = withoutSession("strings") { implicit request =>
    val lang = request.acceptLanguages.find(l => msgs.keySet.contains(l.code)).map(_.code).getOrElse("en")
    Future.successful(Ok(responses(lang)).as("application/javascript"))
  }
}
