package controllers.graphql

import controllers.BaseController
import services.connection.ConnectionSettingsService
import services.graphql.GraphQLService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class SchemaController @javax.inject.Inject() (override val ctx: ApplicationContext, svc: GraphQLService) extends BaseController {
  def render(connection: String) = withSession("schema.render") { implicit request =>
    Future.successful(ConnectionSettingsService.connFor(connection) match {
      case Some(c) => Ok(svc.getConnectionSchema(request.identity, c.id)._2.renderedSchema)
      case None => Redirect(controllers.routes.HomeController.home())
    })
  }

  def voyager(connection: String) = withSession("schema.render") { implicit request =>
    Future.successful(ConnectionSettingsService.connFor(connection) match {
      case Some(c) => Ok(views.html.graphql.voyager(request.identity, c))
      case None => Redirect(controllers.routes.HomeController.home())
    })
  }
}
