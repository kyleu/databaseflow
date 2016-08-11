package controllers.admin

import java.util.UUID

import controllers.BaseController
import models.ddl.DdlQueries
import services.database.core.ResultCacheDatabase
import services.result.CachedResultService
import services.user.UserSearchService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class ResultCacheController @javax.inject.Inject() (override val ctx: ApplicationContext, userService: UserSearchService) extends BaseController {
  def results = withAdminSession("admin-results") { implicit request =>
    val rows = CachedResultService.getAll
    val tables = CachedResultService.getTables
    Future.successful(Ok(views.html.admin.cache.results(request.identity, ctx.config.debug, rows, tables, userService)))
  }

  def removeResult(id: UUID) = withAdminSession("admin-remove-result") { implicit request =>
    CachedResultService.remove(id)
    Future.successful(Redirect(controllers.admin.routes.ResultCacheController.results()).flashing("success" -> messagesApi("result.remove.confirm", id)))
  }

  def removeOrphan(id: String) = withAdminSession("admin-remove-orphan") { implicit request =>
    ResultCacheDatabase.conn.executeUpdate(DdlQueries.DropTable(id)(ResultCacheDatabase.conn.engine))
    Future.successful(Redirect(controllers.admin.routes.ResultCacheController.results()).flashing("success" -> messagesApi("result.remove.orphan", id)))
  }
}
