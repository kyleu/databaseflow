package models.result

import models.QueryResultResponse
import models.graphql.CommonSchema._
import models.graphql.GraphQLContext
import models.query.{QueryResult, SavedQuery, SharedResult}
import models.schema.SchemaGraphQL._
import models.connection.ConnectionGraphQL.permissionEnum
import sangria.macros.derive._

object QueryResultGraphQL {
  implicit val resultColType = deriveObjectType[GraphQLContext, QueryResult.Col](ObjectTypeDescription("A column for this query result."))
  implicit val resultSourceType = deriveObjectType[GraphQLContext, QueryResult.Source](ObjectTypeDescription("The source of this query's results."))
  implicit val resultType = deriveObjectType[GraphQLContext, QueryResult](ObjectTypeDescription("A query result with full metadata."))
  implicit val resultResponseType = deriveObjectType[GraphQLContext, QueryResultResponse](ObjectTypeDescription("A wrapper for metadata and query results."))

  //implicit val savedQueryType = deriveObjectType[GraphQLContext, SavedQuery](ObjectTypeDescription("A query that has been saved for later use."))
  implicit val sharedResultType = deriveObjectType[GraphQLContext, SharedResult](ObjectTypeDescription("A query result that has been shared with others."))
}
