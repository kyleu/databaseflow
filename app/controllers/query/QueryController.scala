package controllers.query

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.mohiva.play.silhouette.api.HandlerResult
import controllers.BaseController
import models.{RequestMessage, ResponseMessage}
import util.FutureUtils.defaultContext
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.connection.ConnectionSettingsService
import services.socket.SocketService
import util.ApplicationContext
import util.web.MessageFrameFormatter

import scala.concurrent.Future

@javax.inject.Singleton
class QueryController @javax.inject.Inject() (
    override val ctx: ApplicationContext,
    implicit val system: ActorSystem,
    implicit val materializer: Materializer
) extends BaseController {

  private[this] implicit val t = new MessageFrameFormatter(ctx.config.debug).transformer

  def main(connection: String) = withSession(s"connection-$connection") { implicit request =>
    val connOpt = ConnectionSettingsService.connFor(connection)
    val activeDb = connOpt.map(c => (c.name, c.id, c.engine.cap.transactionsSupported))
    Future.successful(activeDb match {
      case Some((name, id, txSupported)) => Ok(views.html.query.main(request.identity, ctx.config.debug, id, name, txSupported))
      case None => Redirect(controllers.routes.HomeController.home())
    })
  }

  def connect(connectionId: UUID) = WebSocket.acceptOrResult[RequestMessage, ResponseMessage] { request =>
    implicit val req = Request(request, AnyContentAsEmpty)
    def messages(key: String, args: Any*) = messagesApi.apply(key, args: _*)(request.lang)
    ctx.silhouette.SecuredRequestHandler { securedRequest =>
      Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
    }.map {
      case HandlerResult(_, Some(user)) => Right(ActorFlow.actorRef { out =>
        SocketService.props(None, ctx.supervisor, connectionId, user, out, request.remoteAddress, messages)
      })
      case HandlerResult(_, None) => Left(Redirect(controllers.routes.HomeController.home()))
    }
  }
}
