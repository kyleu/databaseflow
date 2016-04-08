package controllers

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.Materializer
import models.{ RequestMessage, ResponseMessage }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.streams.ActorFlow
import play.api.mvc.{ AnyContentAsEmpty, Request, WebSocket }
import services.connection.ConnectionService
import services.database.ConnectionSettingsService
import utils.ApplicationContext
import utils.web.MessageFrameFormatter

import scala.concurrent.Future

@javax.inject.Singleton
class QueryController @javax.inject.Inject() (
    override val ctx: ApplicationContext,
    implicit val system: ActorSystem,
    implicit val materializer: Materializer
) extends BaseController {
  def main(connectionId: UUID) = withSession(s"connection-$connectionId") { implicit request =>
    val activeDb = ConnectionSettingsService.getById(connectionId).map(c => c.name -> c.id)
    Future.successful(Ok(views.html.query.main(userFor(request), ctx.config.debug, activeDb.map(_._1).getOrElse("..."), UUID.randomUUID)))
  }

  val mff = new MessageFrameFormatter(ctx.config.debug)
  import mff.transformer

  def connect(connectionId: UUID) = WebSocket.tryAcceptWithActor[RequestMessage, ResponseMessage] { request =>
    val userOpt = None
    Future.successful(
      Right(ActorFlow.actorRef(out => ConnectionService.props(None, ctx.supervisor, connectionId, userOpt, out, request.remoteAddress)))
    )
  }
}
