package models.connection

import models.engine.DatabaseEngine
import models.graphql.{CommonGraphQL, GraphQLContext}
import models.graphql.CommonGraphQL._
import models.result.QueryResultGraphQL
import models.schema.SchemaGraphQL
import models.user.Permission
import sangria.macros.derive._
import sangria.schema._
import services.connection.ConnectionSettingsService
import services.query.{SavedQueryService, SharedResultService, SimpleQueryService}
import services.schema.SchemaService

import scala.concurrent.Future

object ConnectionGraphQL {
  val idArg = Argument("id", OptionInputType(CommonGraphQL.uuidType), description = "Filters the results to a connection matching the provided id.")
  val nameArg = Argument("name", OptionInputType(StringType), description = "Filters the results to models with a matching name (case-insensitive).")
  val sqlArg = Argument("sql", StringType, description = "The sql statement you wish to execute.")

  implicit val permissionEnum = CommonGraphQL.deriveEnumeratumType(
    name = "Permission",
    description = "The role of the system user.",
    values = Permission.values.map(t => t -> t.entryName).toList
  )

  implicit val databaseEngineEnum = CommonGraphQL.deriveEnumeratumType(
    name = "DatabaseEngine",
    description = "The database engine used by this connection.",
    values = DatabaseEngine.values.map(t => t -> t.entryName).toList
  )

  implicit val connectionSettingsType = deriveObjectType[GraphQLContext, ConnectionSettings](
    ObjectTypeDescription("Information about the current session."),
    ExcludeFields("password"),
    AddFields(
      Field(
        name = "schema",
        description = Some("Returns the database schema that defines this connection."),
        fieldType = SchemaGraphQL.schemaType,
        resolve = c => SchemaService.getSchemaFor(c.ctx.user, c.value)
      ),
      Field(
        name = "sharedResult",
        description = Some("Returns the results that have been shared for this connection."),
        fieldType = ListType(QueryResultGraphQL.sharedResultType),
        resolve = c => SharedResultService.getForUser(c.ctx.user, c.value.id, None)
      ),
      Field(
        name = "savedQuery",
        description = Some("Returns the saved queries available for this connection."),
        fieldType = ListType(QueryResultGraphQL.savedQueryType),
        resolve = c => SavedQueryService.getForUser(c.ctx.user, c.value.id, None)
      ),
      Field(
        name = "query",
        description = Some("Runs the provided sql query and returns the result."),
        fieldType = QueryResultGraphQL.resultResponseType,
        arguments = sqlArg :: Nil,
        resolve = c => SimpleQueryService.runQuery(c.ctx.user, c.value, c.arg(sqlArg))
      )
    )
  )

  val queryFields = fields[GraphQLContext, Unit](
    Field(
      name = "connection",
      description = Some("Returns information about the available database connections."),
      fieldType = ListType(connectionSettingsType),
      arguments = ConnectionGraphQL.idArg :: nameArg :: Nil,
      resolve = c => Future.successful(ConnectionSettingsService.getVisible(c.ctx.user, c.arg(ConnectionGraphQL.idArg), c.arg(ConnectionGraphQL.nameArg)))
    )
  )

  def queryFieldsForConnection(cs: ConnectionSettings) = fields[GraphQLContext, Unit](
    Field(
      name = "schema",
      description = Some("Returns the database schema that defines this connection."),
      fieldType = SchemaGraphQL.schemaType,
      resolve = c => SchemaService.getSchemaFor(c.ctx.user, cs)
    ),
    Field(
      name = "query",
      description = Some("Runs the provided sql query and returns the result."),
      fieldType = QueryResultGraphQL.resultResponseType,
      arguments = sqlArg :: Nil,
      resolve = c => SimpleQueryService.runQuery(c.ctx.user, cs, c.arg(sqlArg))
    )
  )
}
