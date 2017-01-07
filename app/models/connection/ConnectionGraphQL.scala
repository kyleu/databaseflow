package models.connection

import models.engine.DatabaseEngine
import models.graphql.{CommonSchema, GraphQLContext}
import models.graphql.CommonSchema._
import models.schema.SchemaGraphQL
import models.user.Permission
import sangria.macros.derive._
import sangria.schema._
import services.connection.ConnectionSettingsService
import services.schema.SchemaService

import scala.concurrent.Future

object ConnectionGraphQL {
  val idArg = Argument("id", OptionInputType(CommonSchema.uuidType), description = "Filters the results to a connection matching the provided id.")
  val nameArg = Argument("name", OptionInputType(StringType), description = "Filters the results to a connection matching the provided name.")

  implicit val permissionEnum = CommonSchema.deriveEnumeratumType(
    name = "Permission",
    description = "The role of the system user.",
    values = Permission.values.map(t => t -> t.entryName).toList
  )

  implicit val databaseEngineEnum = CommonSchema.deriveEnumeratumType(
    name = "DatabaseEngine",
    description = "The database engine used by this connection.",
    values = DatabaseEngine.values.map(t => t -> t.entryName).toList
  )

  implicit val connectionSettingsType = deriveObjectType[GraphQLContext, ConnectionSettings](
    ObjectTypeDescription("Information about the current session."),
    ExcludeFields("password"),
    AddFields(Field(
      name = "schema",
      description = Some("Returns the database schema that defines this connection."),
      fieldType = SchemaGraphQL.schemaType,
      resolve = c => Future.successful(SchemaService.getSchemaFor(c.ctx.user, c.value))
    ))
  )

  val queryFields = fields[GraphQLContext, Unit](
    Field(
      name = "connection",
      description = Some("Returns information about the available database connections."),
      arguments = ConnectionGraphQL.idArg :: ConnectionGraphQL.nameArg :: Nil,
      fieldType = ListType(connectionSettingsType),
      resolve = c => Future.successful(ConnectionSettingsService.getVisible(c.ctx.user, c.arg(ConnectionGraphQL.idArg), c.arg(ConnectionGraphQL.nameArg)))
    )
  )
}
