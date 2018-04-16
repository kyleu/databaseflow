package controllers.graphql

import controllers.BaseController
import models.{ResponseMessage, SchemaResponse}
import services.connection.ConnectionSettingsService
import services.graphql.GraphQLService
import services.schema.SchemaService
import util.FutureUtils.defaultContext
import util.ApplicationContext
import util.JsonSerializers._

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

  def json(connection: String) = withSession("schema.json") { implicit request =>
    ConnectionSettingsService.connFor(connection) match {
      case Some(c) => SchemaService.getSchemaWithDetails(None, c).map { s =>
        val jsVal = SchemaResponse(s).asInstanceOf[ResponseMessage].asJson.spaces2
        Ok(jsVal).as("application/json")
      }
      case None => Future.successful(Redirect(controllers.routes.HomeController.home()))
    }
  }
}
