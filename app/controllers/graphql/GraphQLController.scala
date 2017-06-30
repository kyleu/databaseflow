package controllers.graphql

import java.util.UUID

import controllers.BaseController
import models.user.User
import utils.FutureUtils.defaultContext
import play.api.libs.json._
import sangria.execution.{ErrorWithResolver, QueryAnalysisError}
import sangria.marshalling.playJson._
import sangria.parser.SyntaxError
import services.connection.ConnectionSettingsService
import services.graphql.{GraphQLQueryService, GraphQLService}
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class GraphQLController @javax.inject.Inject() (override val ctx: ApplicationContext, svc: GraphQLService) extends BaseController {
  def graphql(connection: String, id: Option[UUID]) = withSession("graphql.ui") { implicit request =>
    Future.successful(ConnectionSettingsService.connFor(connection) match {
      case Some(c) =>
        val list = GraphQLQueryService.getVisible(request.identity, Some(c.id), None, None)
        val q = id.flatMap(i => GraphQLQueryService.getById(i, Some(request.identity)))
        Ok(views.html.graphql.graphiql(request.identity, c.id, c.slug, list, q))
      case None => Redirect(controllers.routes.HomeController.home())
    })
  }

  def graphqlQuery(connection: String, q: String) = withSession("graphql.query") { implicit request =>
    ConnectionSettingsService.connFor(connection) match {
      case Some(c) => execute(Json.parse(q), request.identity, c.id)
      case None => Future.successful(Redirect(controllers.routes.HomeController.home()))
    }
  }

  def graphqlBody(connectionId: UUID) = withSession("graphql.post") { implicit request =>
    val q = request.body.asJson.getOrElse(throw new IllegalStateException("Missing JSON body."))
    execute(q, request.identity, connectionId)
  }

  private[this] def execute(query: JsValue, user: User, connectionId: UUID) = {
    try {
      val f = svc.execute(query, user, connectionId)
      f.map(Ok(_)).recover {
        case error: QueryAnalysisError => BadRequest(error.resolveError)
        case error: ErrorWithResolver => InternalServerError(error.resolveError)
      }
    } catch {
      case error: SyntaxError => Future.successful(BadRequest(Json.obj(
        "syntaxError" -> error.getMessage,
        "locations" -> Json.arr(Json.obj(
          "line" -> error.originalError.position.line,
          "column" -> error.originalError.position.column
        ))
      )))
    }
  }
}
