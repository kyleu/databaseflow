package services.graphql

import java.util.UUID

import models.connection.ConnectionSettings
import models.graphql.{ConnectionGraphQLSchema, GraphQLContext}
import models.user.User
import util.FutureUtils.defaultContext
import io.circe.Json
import io.circe.parser._
import sangria.execution.{Executor, HandledException, QueryReducer}
import sangria.marshalling.circe._
import sangria.parser.QueryParser
import services.connection.ConnectionSettingsService
import util.{ApplicationContext, Logging}
import sangria.schema.Schema
import sangria.validation.QueryValidator

import scala.util.{Failure, Success}

object GraphQLService extends Logging {
  protected val exceptionHandler: Executor.ExceptionHandler = {
    case (_, e: IllegalStateException) =>
      log.warn("Error encountered while running GraphQL query.", e)
      HandledException(message = e.getMessage, additionalFields = Map.empty)
  }

  def parseVariables(variables: String) = if (variables.trim == "" || variables.trim == "null") {
    Json.obj()
  } else {
    parse(variables).right.get
  }
}

@javax.inject.Singleton
case class GraphQLService @javax.inject.Inject() (app: ApplicationContext) {
  private[this] val schemas = collection.mutable.HashMap.empty[UUID, (ConnectionSettings, ConnectionGraphQLSchema)]
  private[this] val rejectComplexQueries = QueryReducer.rejectComplexQueries[Any](1000, (_, _) => new IllegalArgumentException(s"Query is too complex."))

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

  def execute(json: Json, user: User, connectionId: UUID) = {
    val body = json.asObject.get.filter(x => x._1 != "variables").toMap
    val query = body("query").as[String].getOrElse("{}")
    val operation = body.get("operationName").flatMap(_.asString)

    val variables = body.get("variables").map { x =>
      x.asString.map(GraphQLService.parseVariables).getOrElse(x)
    }

    val schema = getConnectionSchema(user, connectionId)._2.schema

    executeQuery(schema, query, variables, operation, user)
  }

  private[this] def executeQuery(schema: Schema[GraphQLContext, Unit], query: String, variables: Option[Json], operation: Option[String], user: User) = {
    QueryParser.parse(query) match {
      case Success(ast) => Executor.execute(
        schema = schema,
        queryAst = ast,
        userContext = GraphQLContext(app, user),
        operationName = operation,
        variables = variables.getOrElse(Json.obj()),
        //deferredResolver = schema.resolver,
        exceptionHandler = GraphQLService.exceptionHandler,
        maxQueryDepth = Some(10),
        queryValidator = QueryValidator.default,
        queryReducers = List(rejectComplexQueries)
      )
      case Failure(error) => throw error
    }
  }

  def parseVariables(variables: String) = if (variables.trim == "" || variables.trim == "null") {
    Json.obj()
  } else {
    parse(variables).right.get
  }
}
