package controllers

import java.util.UUID

import models.audit.AuditType
import models.connection.ConnectionSettings
import models.engine.DatabaseEngine
import models.forms.ConnectionForm
import models.user.Role
import services.audit.AuditRecordService
import services.connection.ConnectionSettingsService
import services.settings.SettingsService
import utils.{ApplicationContext, PasswordEncryptUtils}

import scala.concurrent.Future

@javax.inject.Singleton
class ConnectionSettingsController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def addNew() = withSession("add-new") { implicit request =>
    val conn = ConnectionSettings.empty
    Future.successful(Ok(views.html.connection.form(request.identity, conn, "New Connection", isNew = true)))
  }

  def editForm(id: UUID) = withSession("edit-form-" + id) { implicit request =>
    val addConnectionRole = Role.withName(SettingsService(models.settings.SettingKey.AddConnectionRole))
    if (request.identity.exists(_.role.qualifies(addConnectionRole))) {
      val conn = ConnectionSettingsService.getById(id).getOrElse {
        throw new IllegalArgumentException(s"Invalid connection [$id].")
      }
      Future.successful(Ok(views.html.connection.form(request.identity, conn, conn.name, isNew = false)))
    } else {
      Future.successful(Redirect(routes.HomeController.home()).flashing("error" -> "You do not have permission to add a database connection."))
    }
  }

  def save(connectionId: UUID) = withSession("save") { implicit request =>
    val addConnectionRole = Role.withName(SettingsService(models.settings.SettingKey.AddConnectionRole))
    if (request.identity.exists(_.role.qualifies(addConnectionRole))) {
      val connOpt = ConnectionSettingsService.getById(connectionId)
      val conn = connOpt match {
        case Some(c) => c
        case None => ConnectionSettings(id = connectionId)
      }
      val result = ConnectionForm.form.bindFromRequest.fold(
        formWithErrors => {
          val title = formWithErrors.value.map(_.name).getOrElse("New Connection")
          BadRequest(views.html.connection.form(request.identity, conn, title, isNew = connOpt.isEmpty, formWithErrors.errors))
        },
        cf => {
          val almostUpdated = conn.copy(
            name = cf.name,
            owner = conn.owner.orElse(request.identity.map(_.id)),
            read = cf.read,
            edit = cf.edit,
            engine = DatabaseEngine.get(cf.engine),
            url = cf.url,
            username = cf.username
          )
          val updated = if (cf.password.trim.isEmpty) {
            almostUpdated.copy(password = PasswordEncryptUtils.encrypt(conn.password))
          } else {
            almostUpdated.copy(password = PasswordEncryptUtils.encrypt(cf.password))
          }
          connOpt match {
            case Some(existing) =>
              ConnectionSettingsService.update(updated)
              AuditRecordService.create(AuditType.EditConnection, request.identity.map(_.id), None, Some(updated.id.toString))
            case None =>
              ConnectionSettingsService.insert(updated)
              AuditRecordService.create(AuditType.CreateConnection, request.identity.map(_.id), None, Some(updated.id.toString))
          }
          Redirect(routes.QueryController.main(connectionId))
        }
      )
      Future.successful(result)
    } else {
      Future.successful(Redirect(routes.HomeController.home()).flashing("error" -> "You do not have permission to add a database connection."))
    }
  }

  def delete(connectionId: UUID) = withSession("delete") { implicit request =>
    ConnectionSettingsService.delete(connectionId)
    AuditRecordService.create(AuditType.DeleteConnection, request.identity.map(_.id), None, Some(connectionId.toString))
    Future.successful(Redirect(routes.HomeController.home()))
  }
}
