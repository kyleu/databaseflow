package controllers.admin

import controllers.BaseController
import models.settings.ExportModel
import models.user.Role
import play.api.libs.json.Json
import services.connection.ConnectionSettingsService
import services.query.SavedQueryService
import services.settings.SettingsService
import services.user.UserService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class SystemExportController @javax.inject.Inject() (override val ctx: ApplicationContext, userService: UserService) extends BaseController {
  def systemExport = withAdminSession("admin-system-export") { implicit request =>
    val settings = SettingsService.getOverrides
    val users = request.identity match {
      case Some(u) if u.role == Role.Admin => userService.getAll
      case _ => Nil
    }
    val connections = ConnectionSettingsService.getVisible(request.identity).map(c => c.copy(password = ""))
    val savedQueries = SavedQueryService.getVisible(request.identity)
    val ret = ExportModel(settings, users, connections, savedQueries)

    val json = ret.toString

    Future.successful(Ok(json))
  }

  def systemImport = withAdminSession("admin-system-import") { implicit request =>
    Future.successful(Ok("Imported!"))
  }
}
