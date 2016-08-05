package controllers.admin

import controllers.BaseController
import models.settings.ExportModel
import models.user.Role
import services.connection.ConnectionSettingsService
import services.query.SavedQueryService
import services.settings.SettingsService
import services.user.UserService
import upickle.Js
import utils.ApplicationContext
import upickle.default._

import scala.concurrent.Future

@javax.inject.Singleton
class SystemExportController @javax.inject.Inject() (override val ctx: ApplicationContext, userService: UserService) extends BaseController {
  def systemExport = withAdminSession("admin-system-export") { implicit request =>
    val settings = SettingsService.getOverrides
    val connections = ConnectionSettingsService.getVisible(request.identity).map(c => c.copy(password = ""))
    val savedQueries = SavedQueryService.getVisible(request.identity.id)
    val ret = ExportModel(settings, connections, savedQueries)

    val json = writeJs(ret)

    Future.successful(Ok(json.toString).as("application/json"))
  }

  def systemImport = withAdminSession("admin-system-import") { implicit request =>
    val json = request.body.asText.getOrElse(throw new IllegalArgumentException("Missing JSON body."))
    val export = read[ExportModel](json)
    Future.successful(Ok("Imported!"))
  }
}
