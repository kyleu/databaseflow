package controllers

import java.util.UUID

import models.engine.{ ConnectionSettings, DatabaseEngine }
import models.forms.ConnectionForm
import models.queries.connection.ConnectionQueries
import services.database.MasterDatabase
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class ConnectionController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def addNew() = withSession("add-new") { implicit request =>
    val conn = ConnectionSettings.empty
    Future.successful(Ok(views.html.connection.form(request.identity, conn, "New Connection")))
  }

  def editForm(id: UUID) = withSession("edit-form-" + id) { implicit request =>
    val conn = MasterDatabase.db.query(ConnectionQueries.getById(id)).getOrElse {
      throw new IllegalArgumentException(s"Invalid connection [$id].")
    }
    Future.successful(Ok(views.html.connection.form(request.identity, conn, conn.name)))
  }

  def save(connectionId: UUID) = withSession("save") { implicit request =>
    val connOpt = MasterDatabase.db.query(ConnectionQueries.getById(connectionId))
    val conn = connOpt.getOrElse(ConnectionSettings.empty.copy(id = connectionId))
    val result = ConnectionForm.form.bindFromRequest.fold(
      formWithErrors => {
        val title = ConnectionForm.form.value.map(_.name).getOrElse("New Connection")
        BadRequest(views.html.connection.form(request.identity, conn, title, formWithErrors.errors))
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
          almostUpdated
        } else {
          almostUpdated.copy(password = cf.password)
        }
        connOpt match {
          case Some(existing) => MasterDatabase.db.execute(ConnectionQueries.Update(updated))
          case None => MasterDatabase.db.execute(ConnectionQueries.insert(updated))
        }
        Redirect(routes.QueryController.main(connectionId))
      }
    )
    Future.successful(result)
  }
}
