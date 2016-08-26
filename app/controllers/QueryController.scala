package controllers

import java.io.FileOutputStream
import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.mohiva.play.silhouette.api.HandlerResult
import models.engine.EngineQueries
import models.queries.export.{CsvExportQuery, XlsxExportQuery}
import models.query.QueryResult
import models.{RequestMessage, ResponseMessage}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.config.ConfigFileService
import services.connection.ConnectionSettingsService
import services.database.DatabaseRegistry
import services.database.core.ResultCacheDatabase
import services.socket.SocketService
import utils.web.{FormUtils, MessageFrameFormatter}
import utils.{ApplicationContext, DateUtils}

import scala.concurrent.Future
import scala.util.control.NonFatal
import upickle.default._

@javax.inject.Singleton
class QueryController @javax.inject.Inject() (
    override val ctx: ApplicationContext,
    implicit val system: ActorSystem,
    implicit val materializer: Materializer
) extends BaseController {

  private[this] implicit val t = new MessageFrameFormatter(ctx.config.debug).transformer

  def main(connectionId: UUID) = withSession(s"connection-$connectionId") { implicit request =>
    val activeDb = ConnectionSettingsService.getById(connectionId).map(c => (c.name, c.id, c.engine.cap.transactionsSupported))
    Future.successful(activeDb match {
      case Some((name, id, txSupported)) => Ok(views.html.query.main(request.identity, ctx.config.debug, id, name, txSupported))
      case None => Redirect(routes.HomeController.home())
    })
  }

  def connect(connectionId: UUID) = WebSocket.acceptOrResult[RequestMessage, ResponseMessage] { request =>
    implicit val req = Request(request, AnyContentAsEmpty)
    def messages(key: String, args: Any*) = messagesApi.apply(key, args: _*)
    ctx.silhouette.SecuredRequestHandler { securedRequest =>
      Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
    }.map {
      case HandlerResult(r, Some(user)) => Right(ActorFlow.actorRef { out =>
        SocketService.props(None, ctx.supervisor, connectionId, user, out, request.remoteAddress, messages)
      })
    }
  }

  def export(connectionId: UUID) = withSession("export") { implicit request =>
    val form = FormUtils.getForm(request)

    val sourceJson = form.getOrElse("source", throw new IllegalArgumentException(messagesApi("error.missing.parameter", "resultId")))
    val source = read[QueryResult.Source](sourceJson)
    val format = form.getOrElse("format", throw new IllegalArgumentException(messagesApi("error.missing.parameter", "format")))
    val filename = form.getOrElse("filename", throw new IllegalArgumentException(messagesApi("error.missing.parameter", "filename")))

    val db = if (source.t == "cache") {
      ResultCacheDatabase.conn
    } else {
      DatabaseRegistry.databaseForUser(request.identity, connectionId) match {
        case Right(x) => x
        case Left(x) => throw x
      }
    }

    val ts = DateUtils.now.toString("yyyy-MM-dd")
    val finalName = s"${utils.Config.projectName} $filename Export $ts"

    val file = ConfigFileService.getTempFile(finalName, format)
    val fos = new FileOutputStream(file)
    val sql = EngineQueries.selectFrom(source.name, source.asRowDataOptions)(db.engine)
    val query = format match {
      case "csv" => CsvExportQuery(sql, format, fos)
      case "xlsx" => XlsxExportQuery(filename, sql, format, fos)
      case _ => throw new IllegalArgumentException(messagesApi("error.unknown.format", format))
    }
    try {
      db.query(query)
      fos.close()

      Future.successful(Ok.sendFile(content = file, fileName = (f) => s"$finalName.$format", onClose = () => {
        file.delete
      }))
    } catch {
      case NonFatal(ex) =>
        file.delete
        log.warn(s"Unable to export query for source [$sourceJson].", ex)
        Future.successful(InternalServerError(messagesApi("error.exception.encountered", ex.getClass.getSimpleName, ex.getMessage)))
    }
  }
}
