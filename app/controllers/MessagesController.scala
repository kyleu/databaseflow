package controllers

import java.net.URL

import play.api.i18n.Messages
import util.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class MessagesController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  private[this] def parseMsgs(url: URL) = Messages.parse(Messages.UrlMessageSource(url), url.toString).fold(e => throw e, identity)

  private[this] lazy val msgs = parseMsgs(getClass.getClassLoader.getResource("client/messages"))

  private[this] val responses = {
    val vals = msgs.map { m =>
      s""""${m._1}": "${m._2}""""
    }.mkString(",\n  ")
    s"""window.messages = {\n  $vals\n}"""
  }

  def strings() = withoutSession("strings") { implicit request =>
    Future.successful(Ok(responses).as("application/javascript"))
  }
}
