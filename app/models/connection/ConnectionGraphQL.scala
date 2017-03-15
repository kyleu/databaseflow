package models.connection

import models.graphql.{CommonGraphQL, GraphQLContext}
import models.graphql.CommonGraphQL._
import models.result.QueryResultGraphQL
import models.schema.SchemaGraphQL
import models.user.Permission
import sangria.schema._
import services.graphql.ExploreService
import services.query.{SavedQueryService, SharedResultService, SimpleQueryService}
import services.schema.SchemaService
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object ConnectionGraphQL {
  val nameArg = Argument("name", OptionInputType(StringType), description = "Filters the results to models with a matching name (case-insensitive).")
  val sqlArg = Argument("sql", StringType, description = "The sql statement you wish to execute.")

  implicit val permissionEnum = CommonGraphQL.deriveEnumeratumType(
    name = "Permission",
    description = "The role of the system user.",
    values = Permission.values.map(t => t -> t.entryName).toList
  )

  def queryFieldsForConnection(cs: ConnectionSettings) = fields[GraphQLContext, Unit](
    Field(
      name = "query",
      description = Some("Runs the provided sql query and returns the result."),
      fieldType = QueryResultGraphQL.resultResponseType,
      arguments = sqlArg :: Nil,
      resolve = c => SimpleQueryService.runQuery(c.ctx.user, cs, c.arg(sqlArg))
    ),
    Field(
      name = "explore",
      description = Some("Database objects in an easily-explored graph for this connection."),
      fieldType = ExploreService.exploreType(cs),
      resolve = c => ExploreService.resolve(c.ctx.user, cs)
    ),
    Field(
      name = "schema",
      description = Some("Returns the database schema that defines this connection."),
      fieldType = SchemaGraphQL.schemaType,
      resolve = c => SchemaService.getSchemaWithDetailsFor(c.ctx.user, cs)
    ),
    Field(
      name = "sharedResult",
      description = Some("Returns the results that have been shared for this connection."),
      fieldType = ListType(QueryResultGraphQL.sharedResultType),
      arguments = uuidArg :: nameArg :: Nil,
      resolve = c => SharedResultService.getForUser(c.ctx.user, cs.id, None).map { results =>
        c.arg(uuidArg) match {
          case Some(id) => results.find(_.id == id).toSeq
          case None => c.arg(nameArg) match {
            case Some(name) => results.filter(_.title.toLowerCase.contains(name.toLowerCase))
            case None => results
          }
        }
      }
    ),
    Field(
      name = "savedQuery",
      description = Some("Returns the saved queries available for this connection."),
      fieldType = ListType(QueryResultGraphQL.savedQueryType),
      resolve = c => SavedQueryService.getForUser(c.ctx.user, cs.id, None)
    )
  )
}
