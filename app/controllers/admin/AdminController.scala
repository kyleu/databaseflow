package controllers.admin

import controllers.BaseController
import models.settings.ExportModel
import services.connection.ConnectionSettingsService
import services.query.SavedQueryService
import services.settings.SettingsService
import services.user.UserService
import utils.ApplicationContext
import upickle.default._

import scala.concurrent.Future
import scala.io.Source

@javax.inject.Singleton
class AdminController @javax.inject.Inject() (override val ctx: ApplicationContext, userService: UserService) extends BaseController {
  def index = withAdminSession("admin-index") { implicit request =>
    Future.successful(Ok(views.html.admin.index(request.identity, ctx.config.debug)))
  }

  def systemExport = withAdminSession("admin-system-export") { implicit request =>
    val settings = SettingsService.getOverrides
    val connections = ConnectionSettingsService.getVisible(request.identity).map(c => c.copy(password = ""))
    val savedQueries = SavedQueryService.getVisible(request.identity.id)
    val ret = ExportModel(settings, connections, savedQueries)

    val json = writeJs(ret)

    Future.successful(Ok(json.toString).as("application/json").withHeaders(
      "Content-Disposition" -> "attachment; filename=databaseflow-export.json"
    ))
  }

  def importForm = withAdminSession("admin-system-import-form") { implicit request =>
    Future.successful(Ok(views.html.admin.importForm(request.identity, debug = false)))
  }

  def systemImport = withAdminSession("admin-system-import") { implicit request =>
    request.body.asMultipartFormData.get.file("f").map { f =>
      val json = Source.fromFile(f.ref.file).getLines().mkString("\n")
      val export = read[ExportModel](json)

      export.settings.foreach { s =>
        SettingsService.set(s.key, s.value)
      }

      export.connections.foreach { c =>
        ConnectionSettingsService.insert(c.copy(owner = request.identity.id))
      }

      export.savedQueries.foreach { q =>
        SavedQueryService.save(q.copy(owner = request.identity.id), request.identity.id)
      }

      Future.successful(Redirect(controllers.routes.HomeController.home()).flashing("success" -> "Import successful."))
    }.getOrElse {
      Future.successful(NotFound("Missing file."))
    }
  }
}
