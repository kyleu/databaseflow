package controllers.graphql

import akka.actor.ActorSystem
import controllers.BaseController
import models.graphql.GlobalGraphQLSchema
import models.user.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import sangria.execution.{ErrorWithResolver, QueryAnalysisError}
import sangria.marshalling.playJson._
import sangria.parser.SyntaxError
import sangria.renderer.SchemaRenderer
import services.graphql.{GraphQLFileService, GraphQLService}
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class GraphQLController @javax.inject.Inject() (override val ctx: ApplicationContext, system: ActorSystem) extends BaseController {
  private[this] lazy val renderedSchema = SchemaRenderer.renderSchema(GlobalGraphQLSchema.schema)

  def renderSchema = withSession("graphql.schema") { implicit request =>
    Future.successful(Ok(renderedSchema))
  }

  def graphql(query: Option[String], dir: Option[String], name: Option[String], variables: Option[String]) = {
    withSession("graphql.ui") { implicit request =>
      val vars = variables.getOrElse("{}")
      log.debug(s"Executing GraphQL query [${query.getOrElse("")}] (${dir.getOrElse("-")}:${name.getOrElse("-")}) with [$vars] as variables.")
      Future.successful(Ok(views.html.graphql.graphiql(request.identity, GraphQLFileService.list())))
    }
  }

  def load(dir: Option[String], name: String) = withSession("graphql.load") { implicit request =>
    val q = GraphQLFileService.load(dir, name)
    Future.successful(Redirect(controllers.graphql.routes.GraphQLController.graphql(query = Some(q), dir = dir, name = Some(name)).url))
  }

  def save = withSession("graphql.save") { implicit request =>
    val form = request.body.asFormUrlEncoded.getOrElse(throw new IllegalStateException()).flatMap(x => x._2.headOption.map(y => x._1 -> y))
    val dir = form.get("dir")
    val name = form.getOrElse("name", throw new IllegalStateException("Missing [name]."))
    val body = form.getOrElse("body", throw new IllegalStateException("Missing [body]."))
    GraphQLFileService.save(dir, name, body)
    Future.successful(Redirect(controllers.graphql.routes.GraphQLController.graphql(query = Some(body), dir = dir, name = Some(name))))
  }

  def graphqlBody = withSession("graphql.post") { implicit request =>
    val body = request.body.asJson.getOrElse(throw new IllegalStateException("Missing JSON body."))
    val query = (body \ "query").as[String]
    val operation = (body \ "operationName").asOpt[String]

    val variables = (body \ "variables").toOption.flatMap {
      case JsString(vars) => Some(GraphQLService.parseVariables(vars))
      case obj: JsObject => Some(obj)
      case _ => None
    }

    executeQuery(query, variables, operation, request.identity)
  }

  def executeQuery(query: String, variables: Option[JsObject], operation: Option[String], user: User) = {
    try {
      val f = GraphQLService.executeQuery(ctx, query, variables, operation, user)
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
