package controllers

import java.util.UUID

import models.engine.{ ConnectionSettings, DatabaseEngine }
import models.forms.ConnectionForm
import services.database.ConnectionSettingsService
import utils.{ ApplicationContext, EncryptUtils }

import scala.concurrent.Future

@javax.inject.Singleton
class ConnectionSettingsController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def addNew() = withSession("add-new") { implicit request =>
    val conn = ConnectionSettings.empty
    Future.successful(Ok(views.html.connection.form(request.identity, conn, "New Connection", isNew = true)))
  }

  def editForm(id: UUID) = withSession("edit-form-" + id) { implicit request =>
    val conn = ConnectionSettingsService.getById(id).getOrElse {
      throw new IllegalArgumentException(s"Invalid connection [$id].")
    }
    val isMaster = id == ConnectionSettingsService.masterId
    Future.successful(Ok(views.html.connection.form(request.identity, conn, conn.name, isNew = false)))
  }

  def save(connectionId: UUID) = withSession("save") { implicit request =>
    val connOpt = ConnectionSettingsService.getById(connectionId)
    val conn = connOpt.getOrElse(ConnectionSettings.empty.copy(id = connectionId))
    val result = ConnectionForm.form.bindFromRequest.fold(
      formWithErrors => {
        val title = ConnectionForm.form.value.map(_.name).getOrElse("New Connection")
        BadRequest(views.html.connection.form(request.identity, conn, title, isNew = connOpt.isEmpty, formWithErrors.errors))
      },
      cf => {
        val almostUpdated = conn.copy(
          name = cf.name,
          engine = DatabaseEngine.get(cf.engine),
          url = cf.url,
          username = cf.username,
          password = cf.password
        )
        val updated = if (cf.password.isEmpty) {
          almostUpdated.copy(password = EncryptUtils.encrypt(almostUpdated.password))
        } else {
          almostUpdated.copy(password = EncryptUtils.encrypt(cf.password))
        }
        connOpt match {
          case Some(existing) => ConnectionSettingsService.update(updated)
          case None => ConnectionSettingsService.insert(updated)
        }
        Redirect(routes.QueryController.main(connectionId))
      }
    )
    Future.successful(result)
  }

  def delete(connectionId: UUID) = withSession("delete") { implicit request =>
    ConnectionSettingsService.delete(connectionId)
    Future.successful(Redirect(routes.HomeController.index()))
  }
}
