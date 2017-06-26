package controllers.query

import java.io.ByteArrayOutputStream
import java.util.UUID

import controllers.BaseController
import models.engine.EngineQueries
import models.queries.export.CsvExportQuery
import models.query.QueryResult
import models.user.User
import play.api.i18n.Lang
import services.database.DatabaseRegistry
import services.database.core.ResultCacheDatabase
import services.query.SharedResultService
import upickle.default._
import utils.web.FormUtils
import utils.{ApplicationContext, DateUtils}

import scala.concurrent.Future
import scala.util.control.NonFatal

@javax.inject.Singleton
class ExportController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def exportQuery(connectionId: UUID) = withSession("export") { implicit request =>
    val form = FormUtils.getForm(request)

    val sourceJson = form.getOrElse("source", throw new IllegalArgumentException(messagesApi("error.missing.parameter", "resultId")(
      request.lang(ctx.messagesApi)
    )))
    val source = read[QueryResult.Source](sourceJson)
    val format = form.getOrElse("format", throw new IllegalArgumentException(messagesApi("error.missing.parameter", "format")(request.lang(ctx.messagesApi))))

    send(Some(request.identity), connectionId, source, format, request.lang(ctx.messagesApi))
  }

  def exportShared(id: UUID, format: String) = withoutSession("export.shared") { implicit request =>
    SharedResultService.getById(id) match {
      case Some(sr) =>
        val perm = SharedResultService.canView(request.identity, sr)
        if (perm._1) {
          send(request.identity, sr.connectionId, sr.source, format, request.lang(ctx.messagesApi))
        } else {
          Future.successful(Unauthorized(s"You do not have permission to export the results you requested. ${perm._2}"))
        }
      case None => Future.successful(BadRequest("We couldn't find the results you requested."))
    }
  }

  private[this] def send(user: Option[User], connectionId: UUID, source: QueryResult.Source, format: String, lang: Lang) = {
    val db = if (source.t == "cache") {
      ResultCacheDatabase.conn
    } else {
      user match {
        case Some(u) => DatabaseRegistry.databaseForUser(u, connectionId) match {
          case Right(x) => x
          case Left(x) => throw x
        }
        case None => throw new IllegalStateException("TODO")
      }
    }

    val ts = DateUtils.now.toString("yyyy-MM-dd")
    val finalName = s"${utils.Config.projectId}-export-$ts.$format"

    val os = new ByteArrayOutputStream()
    val (sql, values: Seq[Any]) = EngineQueries.selectFrom(source.name, source.asRowDataOptions(None))(db.engine)
    val (mimeType, query) = format match {
      case "csv" => "text/csv" -> CsvExportQuery(sql, values, os)
      case _ => throw new IllegalArgumentException(messagesApi("error.unknown.format", format)(lang))
    }
    try {
      db.query(query)
      os.close()

      Future.successful(Ok(os.toByteArray).as(mimeType).withHeaders((CONTENT_DISPOSITION, "inline; filename=" + finalName)))
    } catch {
      case NonFatal(ex) =>
        os.close()
        log.warn(s"Unable to export query for source [$source].", ex)
        Future.successful(InternalServerError(messagesApi("error.exception.encountered", ex.getClass.getSimpleName, ex.getMessage)(lang)))
    }
  }
}
