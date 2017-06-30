package services.graphql

import java.util.UUID

import models.connection.ConnectionSettings
import models.graphql.{ConnectionGraphQLSchema, GraphQLContext}
import models.user.User
import utils.FutureUtils.defaultContext
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import sangria.execution.{Executor, HandledException}
import sangria.marshalling.playJson._
import sangria.parser.QueryParser
import sangria.schema.Schema
import services.connection.ConnectionSettingsService
import utils.ApplicationContext

import scala.util.{Failure, Success}

object GraphQLService {
  protected val exceptionHandler: Executor.ExceptionHandler = {
    case (_, e: IllegalStateException) => HandledException(e.getMessage)
  }

  protected def parseVariables(variables: String) = if (variables.trim == "" || variables.trim == "null") {
    Json.obj()
  } else {
    Json.parse(variables).as[JsObject]
  }
}

@javax.inject.Singleton
case class GraphQLService @javax.inject.Inject() (app: ApplicationContext) {
  private[this] val schemas = collection.mutable.HashMap.empty[UUID, (ConnectionSettings, ConnectionGraphQLSchema)]

  def getConnectionSchema(user: User, connectionId: UUID) = {
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

  def execute(body: JsValue, user: User, connectionId: UUID) = {
    val query = (body \ "query").as[String]
    val operation = (body \ "operationName").asOpt[String]

    val variables = (body \ "variables").toOption.flatMap {
      case JsString(vars) => Some(GraphQLService.parseVariables(vars))
      case obj: JsObject => Some(obj)
      case _ => None
    }

    val schema = getConnectionSchema(user, connectionId)._2.schema

    executeQuery(schema, query, variables, operation, user)
  }

  private[this] def executeQuery(schema: Schema[GraphQLContext, Unit], query: String, variables: Option[JsObject], operation: Option[String], user: User) = {
    QueryParser.parse(query) match {
      case Success(ast) => Executor.execute(
        schema = schema,
        queryAst = ast,
        userContext = GraphQLContext(app, user),
        operationName = operation,
        variables = variables.getOrElse(Json.obj()),
        //deferredResolver = GraphQLResolver,
        exceptionHandler = GraphQLService.exceptionHandler,
        maxQueryDepth = Some(10)
      )
      case Failure(error) => throw error
    }
  }
}
