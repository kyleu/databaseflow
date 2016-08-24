package controllers

import java.util.UUID

import models.queries.result.SharedResultQueries
import services.database.core.MasterDatabase
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class SharedResultController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def index() = withoutSession("result.index") { implicit request =>
    val sharedResults = MasterDatabase.conn.query(SharedResultQueries.getAll())
    Future.successful(Ok(views.html.result.list(request.identity, sharedResults, ctx.config.debug)))
  }

  def view(id: UUID) = withoutSession("result.view") { implicit request =>
    MasterDatabase.conn.query(SharedResultQueries.getById(id)) match {
      case Some(sr) => Future.successful(Ok(views.html.result.view(request.identity, sr, ctx.config.debug)))
      case None => Future.successful(BadRequest("We couldn't find the results you requested."))
    }
  }
}
