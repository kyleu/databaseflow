package controllers

import java.util.UUID

import models.connection.ConnectionSettings
import models.forms.GraphQLForm
import models.graphql.{ConnectionGraphQLSchema, GraphQLContext, GraphQLQuery}
import models.user.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import sangria.execution.{ErrorWithResolver, QueryAnalysisError}
import sangria.marshalling.playJson._
import sangria.parser.SyntaxError
import sangria.schema.Schema
import services.connection.ConnectionSettingsService
import services.graphql.{GraphQLQueryService, GraphQLService}
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class GraphQLController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  private[this] val schemas = collection.mutable.HashMap.empty[UUID, (ConnectionSettings, ConnectionGraphQLSchema)]

  private[this] def getConnectionSchema(user: User, connectionId: UUID) = {
    val ret = schemas.getOrElseUpdate(connectionId, {
      val cs = ConnectionSettingsService.getById(connectionId).getOrElse(throw new IllegalStateException(s"Invalid connection [$connectionId]."))
      cs -> ConnectionGraphQLSchema(cs)
    })
    val perms = ConnectionSettingsService.canRead(user, ret._1)
    if (!perms._1) {
      throw new IllegalStateException(perms._2)
    }
    ret
  }

  def renderSchema(connectionId: UUID) = withSession("graphql.schema") { implicit request =>
    Future.successful(Ok(getConnectionSchema(request.identity, connectionId)._2.renderedSchema).as("application/json"))
  }

  def graphql(connectionId: UUID, id: Option[UUID]) = withSession("graphql.ui") { implicit request =>
    val list = GraphQLQueryService.getVisible(request.identity, Some(connectionId), None, None)
    val q = id.flatMap(i => GraphQLQueryService.getById(i, Some(request.identity)))
    Future.successful(Ok(views.html.graphql.graphiql(request.identity, connectionId, list, q)))
  }

  def graphqlBody(connectionId: UUID) = withSession("graphql.post") { implicit request =>
    val body = request.body.asJson.getOrElse(throw new IllegalStateException("Missing JSON body."))
    val query = (body \ "query").as[String]
    val operation = (body \ "operationName").asOpt[String]

    val variables = (body \ "variables").toOption.flatMap {
      case JsString(vars) => Some(GraphQLService.parseVariables(vars))
      case obj: JsObject => Some(obj)
      case _ => None
    }

    val schema = getConnectionSchema(request.identity, connectionId)._2.schema

    executeQuery(query, variables, operation, schema, request.identity)
  }

  def executeQuery(query: String, variables: Option[JsObject], operation: Option[String], schema: Schema[GraphQLContext, Unit], user: User) = {
    try {
      val f = GraphQLService.executeQuery(ctx, schema, query, variables, operation, user)
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

  def load(conn: UUID, queryId: UUID) = withSession("graphql.load") { implicit request =>
    val q = GraphQLQueryService.getById(queryId, Some(request.identity))
    Future.successful(Redirect(controllers.routes.GraphQLController.graphql(conn, Some(queryId)).url))
  }

  def save(conn: UUID) = withSession("graphql.save") { implicit request =>
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
}
