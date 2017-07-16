package controllers.connection

import java.util.UUID

import controllers.BaseController
import models.audit.AuditType
import models.connection.ConnectionSettings
import models.forms.ConnectionForm
import models.user.Role
import play.api.data.FormError
import services.audit.AuditRecordService
import services.connection.ConnectionSettingsService
import services.database.{DatabaseRegistry, SampleDatabaseService}
import services.schema.SchemaService
import services.settings.SettingsService
import util.{ApplicationContext, PasswordEncryptUtils}

import scala.concurrent.Future

@javax.inject.Singleton
class ConnectionSettingsController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def addNew() = withSession("add-new") { implicit request =>
    val conn = ConnectionSettings(UUID.randomUUID, "", "", request.identity.id)
    Future.successful(Ok(views.html.connection.form(request.identity, conn, messagesApi("connection.new.title")(request.lang), isNew = true)))
  }

  def editForm(id: UUID) = withSession("edit-form-" + id) { implicit request =>
    val addConnectionRole = Role.withName(SettingsService(models.settings.SettingKey.AddConnectionRole))
    if (request.identity.role.qualifies(addConnectionRole)) {
      val conn = ConnectionSettingsService.getById(id).getOrElse(throw new IllegalArgumentException(s"Invalid connection [$id]."))
      Future.successful(Ok(views.html.connection.form(request.identity, conn, conn.name, isNew = false)))
    } else {
      Future.successful(Redirect(controllers.routes.HomeController.home()).flashing("error" -> messagesApi("connection.permission.denied")(request.lang)))
    }
  }

  def save(connectionId: UUID) = withSession("save") { implicit request =>
    val addConnectionRole = Role.withName(SettingsService(models.settings.SettingKey.AddConnectionRole))
    if (request.identity.role.qualifies(addConnectionRole)) {
      val connOpt = ConnectionSettingsService.getById(connectionId)
      val conn = connOpt.getOrElse(ConnectionSettings(id = connectionId, name = "", slug = "", owner = request.identity.id))
      val result = ConnectionForm.form.bindFromRequest.fold(
        formWithErrors => {
          val title = formWithErrors.value.map(_.name).getOrElse(messagesApi("connection.new.title")(request.lang))
          BadRequest(views.html.connection.form(request.identity, conn, title, isNew = connOpt.isEmpty, formWithErrors.errors))
        },
        cf => {
          val updated = cf.copyWith(conn).copy(password = if (cf.password.trim.isEmpty) { conn.password } else { PasswordEncryptUtils.encrypt(cf.password) })
          try {
            connOpt match {
              case Some(existing) =>
                ConnectionSettingsService.update(updated)
                DatabaseRegistry.reset(updated) match {
                  case Right(c) =>
                    SchemaService.getSchema(c, forceRefresh = true)
                    AuditRecordService.create(AuditType.EditConnection, request.identity.id, None, Some(updated.id.toString))
                    Redirect(controllers.query.routes.QueryController.main(updated.slug))
                  case Left(x) =>
                    log.warn("Unable to connect to newly saved connection.", x)
                    Redirect(controllers.query.routes.QueryController.main(existing.slug))
                }
              case None =>
                ConnectionSettingsService.insert(updated)
                AuditRecordService.create(AuditType.CreateConnection, request.identity.id, None, Some(updated.id.toString))
                Redirect(controllers.query.routes.QueryController.main(updated.slug))
            }
          } catch {
            case ex: IllegalStateException =>
              val errors = Seq(FormError("Name in.use", Seq(ex.getMessage)))
              BadRequest(views.html.connection.form(request.identity, updated, updated.name, isNew = connOpt.isEmpty, errors))
          }
        }
      )
      Future.successful(result)
    } else {
      Future.successful(Redirect(controllers.routes.HomeController.home()).flashing("error" -> messagesApi("connection.permission.denied")(request.lang)))
    }
  }

  def copyConnection(connectionId: UUID) = withSession("copy") { implicit request =>
    val conn = ConnectionSettingsService.getById(connectionId).getOrElse(throw new IllegalStateException(s"Invalid connection [$connectionId]."))
    val updated = conn.copy(
      id = UUID.randomUUID,
      name = "Copy of " + conn.name,
      password = ""
    )
    Future.successful(Ok(views.html.connection.form(request.identity, updated, updated.name, isNew = true)))
  }

  def delete(connectionId: UUID) = withSession("delete") { implicit request =>
    ConnectionSettingsService.delete(connectionId, request.identity.id)
    Future.successful(Redirect(controllers.routes.HomeController.home()))
  }

  def createSample() = withSession("create.sample") { implicit request =>
    val cs = SampleDatabaseService(request.identity.id)
    Future.successful(Redirect(controllers.query.routes.QueryController.main(cs.slug)))
  }
}
