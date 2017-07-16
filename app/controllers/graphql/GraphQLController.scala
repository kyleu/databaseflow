package controllers.graphql

import java.util.UUID

import controllers.BaseController
import io.circe.Json
import io.circe.parser
import models.user.User
import play.api.libs.json.JsObject
import util.FutureUtils.defaultContext
import sangria.execution.{ErrorWithResolver, QueryAnalysisError}
import sangria.marshalling.circe._
import sangria.marshalling.MarshallingUtil._
import sangria.parser.SyntaxError
import services.connection.ConnectionSettingsService
import services.graphql.GraphQLService
import util.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class GraphQLController @javax.inject.Inject() (override val ctx: ApplicationContext, svc: GraphQLService) extends BaseController {
  def graphql(connection: String, id: Option[UUID]) = withSession("graphql.ui") { implicit request =>
    Future.successful(ConnectionSettingsService.connFor(connection) match {
      case Some(c) => Ok(views.html.graphql.graphiql(request.identity, c))
      case None => Redirect(controllers.routes.HomeController.home())
    })
  }

  def graphqlQuery(connection: String, q: String) = withSession("graphql.query") { implicit request =>
    ConnectionSettingsService.connFor(connection) match {
      case Some(c) => execute(parser.parse(q).right.get, request.identity, c.id)
      case None => Future.successful(Redirect(controllers.routes.HomeController.home()))
    }
  }

  def graphqlBody(connectionId: UUID) = withSession("graphql.post") { implicit request =>
    val json = {
      import sangria.marshalling.playJson._
      val playJson = request.body.asJson.getOrElse(JsObject.empty)
      playJson.convertMarshaled[Json]
    }
    execute(json, request.identity, connectionId)
  }

  private[this] def execute(query: Json, user: User, connectionId: UUID) = {
    try {
      val f = svc.execute(query, user, connectionId)
      f.map(x => Ok(x.spaces2).as("application/json")).recover {
        case error: QueryAnalysisError => BadRequest(error.resolveError.spaces2).as("application/json")
        case error: ErrorWithResolver => InternalServerError(error.resolveError.spaces2).as("application/json")
      }
    } catch {
      case error: SyntaxError =>
        val json = Json.obj(
          "syntaxError" -> Json.fromString(error.getMessage),
          "locations" -> Json.arr(Json.obj(
            "line" -> Json.fromInt(error.originalError.position.line),
            "column" -> Json.fromInt(error.originalError.position.column)
          ))
        )
        Future.successful(BadRequest(json.spaces2).as("application/json"))
    }
  }
}
