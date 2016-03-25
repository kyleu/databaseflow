package controllers

import java.util.UUID

import models.engine.ConnectionSettings
import models.queries.connection.ConnectionQueries
import services.database.MasterDatabase
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class ConnectionController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def addNew() = withSession("add-new") { implicit request =>
    val conn = ConnectionSettings()
    Future.successful(Ok(views.html.connection.form(request.identity, conn)))
  }

  def editForm(id: UUID) = withSession("edit-form-" + id) { implicit request =>
    val conn = MasterDatabase.db.query(ConnectionQueries.getById(id)).getOrElse {
      throw new IllegalArgumentException(s"Invalid connection [$id].")
    }
    Future.successful(Ok(views.html.connection.form(request.identity, conn)))
  }

  def save() = withSession("save") { implicit request =>
    Future.successful(Ok("TODO"))
  }
}
