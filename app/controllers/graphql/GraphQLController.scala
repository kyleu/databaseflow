package controllers.graphql

import java.util.UUID

import akka.actor.ActorSystem
import controllers.BaseController
import models.forms.GraphQLForm
import models.graphql.{GlobalGraphQLSchema, GraphQLQuery}
import models.user.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import sangria.execution.{ErrorWithResolver, QueryAnalysisError}
import sangria.marshalling.playJson._
import sangria.parser.SyntaxError
import sangria.renderer.SchemaRenderer
import services.graphql.{GraphQLQueryService, GraphQLService}
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class GraphQLController @javax.inject.Inject() (override val ctx: ApplicationContext, system: ActorSystem) extends BaseController {
  private[this] lazy val renderedSchema = SchemaRenderer.renderSchema(GlobalGraphQLSchema.schema)

  def renderSchema = withSession("graphql.schema") { implicit request =>
    Future.successful(Ok(renderedSchema))
  }

  def graphql(id: Option[UUID]) = {
    withSession("graphql.ui") { implicit request =>
      val list = GraphQLQueryService.getVisible(request.identity, None, None)
      val q = id.flatMap(i => GraphQLQueryService.getById(i, Some(request.identity)))
      Future.successful(Ok(views.html.graphql.graphiql(request.identity, list, q)))
    }
  }

  def load(id: UUID) = withSession("graphql.load") { implicit request =>
    val q = GraphQLQueryService.getById(id, Some(request.identity))
    Future.successful(Redirect(controllers.graphql.routes.GraphQLController.graphql(id = Some(id)).url))
  }

  def save = withSession("graphql.save") { implicit request =>
    val result = GraphQLForm.form.bindFromRequest.fold(
      formWithErrors => BadRequest(formWithErrors.value.map(_.name).getOrElse(messagesApi("Unknown error"))),
      gqlf => {
        val gqlOpt = gqlf.id.map(id => GraphQLQueryService.getById(id, Some(request.identity)).getOrElse(throw new IllegalStateException("Not allowed.")))
        val gql = gqlOpt.getOrElse(GraphQLQuery.empty(request.identity.id))
        val updated = gql.copy(
          connection = gqlf.connection,
          category = gqlf.category,
          name = gqlf.name,
          query = gqlf.query,
          read = gqlf.read,
          edit = gqlf.edit
        )
        gqlOpt match {
          case Some(existing) =>
            GraphQLQueryService.update(updated, request.identity)
            Ok("Updated")
          case None =>
            GraphQLQueryService.insert(updated)
            Ok("Inserted")
        }
      }
    )
    Future.successful(result)
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
      val f = GraphQLService.executeQuery(ctx, None, query, variables, operation, user)
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
