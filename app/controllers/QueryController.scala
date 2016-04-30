package controllers

import java.io.ByteArrayInputStream
import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.mohiva.play.silhouette.api.HandlerResult
import models.{ RequestMessage, ResponseMessage }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Enumerator
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.connection.{ ConnectionService, ConnectionSettingsService }
import utils.ApplicationContext
import utils.web.MessageFrameFormatter

import scala.concurrent.Future

@javax.inject.Singleton
class QueryController @javax.inject.Inject() (
    override val ctx: ApplicationContext,
    implicit val system: ActorSystem,
    implicit val materializer: Materializer
) extends BaseController {

  private[this] implicit val t = new MessageFrameFormatter(ctx.config.debug).transformer

  def main(connectionId: UUID) = withSession(s"connection-$connectionId") { implicit request =>
    val activeDb = ConnectionSettingsService.getById(connectionId).map(c => c.name -> c.id)
    Future.successful(activeDb match {
      case Some((name, id)) => Ok(views.html.query.main(request.identity, ctx.config.debug, id, name, UUID.randomUUID))
      case None => Redirect(routes.HomeController.index())
    })
  }

  def connect(connectionId: UUID) = WebSocket.acceptOrResult[RequestMessage, ResponseMessage] { request =>
    implicit val req = Request(request, AnyContentAsEmpty)
    ctx.silhouette.UserAwareRequestHandler { userAwareRequest =>
      Future.successful(HandlerResult(Ok, Some(userAwareRequest.identity)))
    }.map {
      case HandlerResult(r, Some(user)) => Right(ActorFlow.actorRef { out =>
        ConnectionService.props(None, ctx.supervisor, connectionId, user, out, request.remoteAddress)
      })
    }
  }

  def export(connectionId: UUID) = withSession("export") { implicit request =>
    val form = request.body.asFormUrlEncoded.getOrElse(throw new IllegalStateException("Invalid request"))

    val queryId = form.get("queryId").flatMap(_.headOption).getOrElse(throw new IllegalArgumentException("Missing [queryId] parameter."))
    val sql = form.get("sql").flatMap(_.headOption).getOrElse(throw new IllegalArgumentException("Missing [sql] parameter."))
    val format = form.get("format").flatMap(_.headOption).getOrElse(throw new IllegalArgumentException("Missing [format] parameter."))

    val status = s"Exporting query [$queryId] in [$format] format using sql [$sql]!, motherfucker!"

    val result = Ok(status)

    val withHeaders = result.withHeaders(
      CONTENT_TYPE -> (format match {
        case "csv" => "text/csv"
        case "xlsx" => "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        case x => throw new IllegalStateException(s"Invalid format [$x].")
      }),
      CONTENT_DISPOSITION -> s"attachment; filename=DatabaseFlowExport.$format"
    )

    Future.successful(withHeaders)
  }
}
